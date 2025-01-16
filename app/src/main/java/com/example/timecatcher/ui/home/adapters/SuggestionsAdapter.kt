package com.example.timecatcher.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.timecatcher.data.model.ActivityItem
import com.example.timecatcher.databinding.ItemSuggestionBinding

class SuggestionsAdapter(
    private val items: List<ActivityItem>,
    private val onDoActivityClicked: (ActivityItem) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class SuggestionViewHolder(private val binding: ItemSuggestionBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: ActivityItem) {
            binding.tvSuggestionTitle.text = activity.title
            binding.tvSuggestionDesc.text = activity.description
            val time = activity.estimatedTime ?: 0
            binding.tvSuggestionTime.text = "$time min"

            binding.btnDoActivity.setOnClickListener {
                onDoActivityClicked(activity)
            }
        }
    }
}