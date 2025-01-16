package com.example.timecatcher.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.timecatcher.data.model.InProgressItem
import com.example.timecatcher.databinding.ItemInProgressBinding

class InProgressAdapter(
    private val items: MutableList<InProgressItem>,
    private val onCompletedClicked: (InProgressItem) -> Unit
) : RecyclerView.Adapter<InProgressAdapter.InProgressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InProgressViewHolder {
        val binding = ItemInProgressBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InProgressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InProgressViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newList: List<InProgressItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class InProgressViewHolder(private val binding: ItemInProgressBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InProgressItem) {
            binding.tvInProgressTitle.text = item.activity.title

            // Calcula tiempo restante (finishTime - currentTime)
            val remainingMillis = item.finishTimeMillis - System.currentTimeMillis()
            val remainingSec = (remainingMillis / 1000).coerceAtLeast(0)
            val min = remainingSec / 60
            val sec = remainingSec % 60
            binding.tvRemainingTime.text = String.format("%02d:%02d", min, sec)

            binding.btnCompleted.setOnClickListener {
                onCompletedClicked(item)
            }
        }
    }
}