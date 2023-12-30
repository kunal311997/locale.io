package com.location.reminder.sound.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.location.reminder.sound.databinding.ItemReminderBinding
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.util.gone
import com.location.reminder.sound.util.visible
import java.time.format.DateTimeFormatter

class TasksAdapter(
    private val tasks: List<Task>,
    private val onItemClick: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) :
    RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {
    inner class TaskViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            if (task.title.isEmpty()) binding.txtTitle.gone()
            else binding.txtTitle.visible()
            if (task.description.isNullOrEmpty()) binding.txtDescription.gone()
            else binding.txtDescription.visible()
            binding.txtTitle.text = task.title
            binding.txtDescription.text = task.description
            binding.txtAddress.text = task.address
            binding.btnSwitch.isChecked = task.isEnabled
            binding.txtTime.text = task.createdAt?.format(DateTimeFormatter.ISO_DATE)
            if (task.destinationSoundMode == null) binding.txtSound.gone()
            else binding.txtSound.visible()
            binding.txtSound.text = task.destinationSoundMode?.name
            binding.imgDelete.setOnClickListener {
                onDelete.invoke(task)
            }
            binding.root.setOnClickListener {
                onItemClick.invoke(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemReminderBinding.inflate(layoutInflater, parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }
}