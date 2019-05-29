from flask import Flask, request, jsonify, json
import time
from timeloop import Timeloop
from datetime import timedelta
from flask_pymongo import PyMongo
from pymongo import UpdateOne
from AuthManager import *

MONEY_TO_BUY = 10
INCOME_PER_CHAIN = 10

WRONG_AUTH_DATA = {
	"response": "wa",
	"message": "Login or password are incorrect",
}

INCORRECT_TOKEN = {
	"response": "wa",
	"message": "The token, which was passed, is incorrect"
}

UNEXPECTED_METHOD = {
	"response": "wa",
	"message": "This method was not expected, try another one. (GET <-> POST)"
}

ACTION_COMPLETE = {
	"response": "ok",
	"message": "The action was completed successfully"
}

NOT_ENOUGH_RESOURCES = {
	"response": "rj",
	"message": "Not enough resources to complete the action"
}

app = Flask(__name__)
app.config["MONGO_URI"] = "mongodb+srv://dbUser:pass@chaincluster-rqpx3.mongodb.net/main"
mongo = PyMongo(app)
auth_manager = AuthManager(mongo)

action_handlers = dict()
action_handlers[1] = "make_contrib"

events = Timeloop()

def keyf(x, total_contrib):
	real_pcent = x / total_contrib
	basic = int(real_pcent * INCOME_PER_CHAIN)
	given_pcent = basic / INCOME_PER_CHAIN
	return -real_pcent


@events.job(interval=timedelta(seconds=60*60*3))
def event_loop():
	chain_contribs = {}
	chain_contributors = {}
	contribs = mongo.db.contribs.find()
	for elem in contribs:
		user_id = elem["user_id"]
		chain_id = elem["chain_id"]

		chain_contribs[chain_id] = (chain_contribs.get(chain_id, 0)) + int(elem["value"])
		if chain_id not in chain_contributors:
			chain_contributors[chain_id] = []
		chain_contributors[chain_id].append(elem)

	for chain in chain_contributors:
		user_result = {}
		users_list = chain_contributors[chain]
		total_contrib = chain_contribs[chain]
		users_list.sort(key=lambda x: keyf(int(x["value"]), total_contrib))
		total = 0
		for user in users_list:
			x = int(user["value"])
			real_pcent = x / total_contrib
			basic = int(real_pcent * INCOME_PER_CHAIN)
			total += basic
			user_result[user["user_id"]] = basic

		for user in users_list[:INCOME_PER_CHAIN - total]:
			user_result[user["user_id"]] = user_result.get(user["user_id"], 0) + 1

		requests = [UpdateOne({"id": user["user_id"]}, {"$inc": {"money": user_result.get(user["user_id"], 0)}}) 
			for user in users_list]
		print(requests)
		try:
			mongo.db.users.bulk_write(requests)
		except Exception as e:
			print(e.details)


events.start()

@app.route("/chains/")
def get_chains():
	auth_passed = auth_manager.check_token(request.args.get("token"))
	if not auth_passed:		
		return jsonify(INCORRECT_TOKEN)
	result = []
	for elem in mongo.db.chains.find():
		elem.pop("_id")
		result.append(elem)
	
	return jsonify(result)

@app.route("/chains/<int:id>")
def get_chain(id):
	auth_passed = auth_manager.check_token(request.args.get("token"))
	if not auth_passed:		
		return jsonify(INCORRECT_TOKEN)

	result = mongo.db.chains.find_one({"id": id})
	if result is None:
		return jsonify({})

	result.pop("_id")
	return jsonify(result)

@app.route("/chains/contrib/<int:id>")
def get_contribs_by_chain(id):
	auth_passed = auth_manager.check_token(request.args.get("token"))
	if not auth_passed:		
		return jsonify(INCORRECT_TOKEN)

	contribs = mongo.db.contribs.find({"chain_id": id})
	result = []
	for elem in contribs:
		user_id = elem["user_id"]
		user_name = mongo.db.users.find_one({"id": user_id})["name"]
		elem.pop("_id")
		elem["user_name"] = user_name
		result.append(elem)
	return jsonify(result)


@app.route("/chains/action/", methods=["GET", "POST"])
def make_action():
	if request.method == "GET":
		return jsonify(UNEXPECTED_METHOD)
	action = request.get_json();
	if action is None:
		return jsonify("bad request")
	if not auth_manager.check_token(action.get("token")):
		return jsonify(INCORRECT_TOKEN)

	handler = globals()[action_handlers.get(action["action_code"])]
	if handler is None:
		return jsonify("bad request")

	return handler(action)

def make_contrib(action):
	# print(auth_manager.get_user(action["token"])["name"])
	user = auth_manager.get_user(action["token"])
	user_id = user["id"]
	chain_id = action["chain_id"]

	user_money = int(user["money"])
	if user_money < MONEY_TO_BUY:
		print(NOT_ENOUGH_RESOURCES)
		return jsonify(NOT_ENOUGH_RESOURCES)

	user_money -= money_to_buy
	user['money'] = str(user_money)


	contrib = mongo.db.contribs.find_one({"user_id": user_id, "chain_id": chain_id})
	if contrib is None:
		contrib = {
			"user_id": user_id,
			"chain_id": chain_id,
			"value": "1"
		}
	else:
		contrib['value'] = str(int(contrib["value"]) + 1)

	mongo.db.users.find_one_and_replace({"id": user_id}, user)
	mongo.db.contribs.find_one_and_replace({"user_id": user_id, "chain_id": chain_id}, contrib, upsert=True)

	return jsonify(ACTION_COMPLETE)



@app.route("/login")
def get_authtoken():
	username = request.args.get("username")
	password = request.args.get("password")
	if (username is None or password is None):
		return jsonify(WRONG_AUTH_DATA)

	user = auth_manager.search(username, password)
	if (user is None):
		return jsonify(WRONG_AUTH_DATA)

	token = auth_manager.acquire_token(user)
	return jsonify(
		{"response": "ok", "user_id": user["user_id"], "token": token}
	)

@app.route("/user")
def get_user():
	user = auth_manager.get_user(request.args["token"])
	if user is None:
		return jsonify(INCORRECT_TOKEN)
	return jsonify(user)

@app.route("/register")
def register():
	username = request.args.get("username")
	name = request.args.get("name")
	password = request.args.get("password")
	if username is None or password is None:
		return jsonify(WRONG_AUTH_DATA)

	user = auth_manager.get_usertoken(username)
	if user is not None:
		return jsonify(WRONG_AUTH_DATA)

	new_usertoken = auth_manager.get_new_usertoken(username, password)
	mongo.db.usertokens.insert_one(new_usertoken)
	new_user = auth_manager.get_new_user(name, new_usertoken["user_id"])
	mongo.db.users.insert_one(new_user)

	return jsonify({"response": "ok"})


@app.route("/")
def hello():
    return "Hello World!"

