package ru.ladybug.isolatedsingularity;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContributorsAdapter extends RecyclerView.Adapter<ContributorsAdapter.ContributorHolder> {
    private List<ChainData.Contributor> contributors = new ArrayList<>();

    public List<ChainData.Contributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<ChainData.Contributor> contributors) {
        this.contributors = contributors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContributorHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item, viewGroup, false);
        return new ContributorHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContributorHolder contributorHolder, int i) {
        ChainData.Contributor currentContributor = contributors.get(i);
        contributorHolder.nameView.setText(currentContributor.getName());
        contributorHolder.contributionView.setText(currentContributor.getContribution().toString());
    }

    @Override
    public int getItemCount() {
        return contributors.size();
    }


    class ContributorHolder extends RecyclerView.ViewHolder {
        private TextView nameView;
        private TextView contributionView;
        public ContributorHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.contributorName);
            contributionView = itemView.findViewById(R.id.contributorContribution);
        }

    }
}
