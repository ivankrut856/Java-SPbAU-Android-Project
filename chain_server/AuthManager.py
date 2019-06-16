import bcrypt
import json
import os
import secrets
import datetime
from bson.json_util import dumps


class AuthManager:

	def get_user(self, token):
		if token is None:
			return None
		token_data = self.mongo.db.tokens.find_one({"token": token})
		user = self.mongo.db.users.find_one({"id": token_data['user_id']})
		user.pop("_id")
		return user

	def get_usertoken(self, username):
		return self.mongo.db.usertokens.find_one({"user_name": username})

	def get_new_usertoken(self, username, password):
		hashed = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())
		next_id = self.mongo.db.usertokens.count_documents({})

		return {"user_name": username, "user_id": next_id, "password": hashed.decode("utf-8")}

	def get_new_user(self, name, id):
		return {"name": name, "id": id, "money": 100}

	def search(self, username, password):
		user = self.mongo.db.usertokens.find_one({"user_name": username})
		if user is None:
			return None

		hashed = user['password'].encode('utf-8')
		encoded = password.encode('utf-8')
		password_correct = (hashed == bcrypt.hashpw(encoded, hashed))

		if not password_correct:
			return None
		return user

	def acquire_token(self, user):
		token = secrets.token_hex(16)
		timestamp = int(datetime.datetime.timestamp(datetime.datetime.now()))

		self.mongo.db.tokens.insert_one({
			"user_id": user['user_id'],
			"token": token,
			"timestamp": str(timestamp)
		})

		return token;  # ; ?

	def check_token(self, token):
		if token is None:
			return False

		token_data = self.mongo.db.tokens.find_one({"token": token})
		
		if token_data is not None:
			return True
		else:
			return False


	def __init__(self, mongo):
		self.mongo = mongo