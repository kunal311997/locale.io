package com.location.reminder.sound.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import com.location.reminder.sound.R
import com.location.reminder.sound.databinding.FragmentRemindersBinding
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.ui.adapters.TasksAdapter
import com.location.reminder.sound.util.gone
import com.location.reminder.sound.util.startLocationUpdateService
import com.location.reminder.sound.util.stopLocationUpdateService
import com.location.reminder.sound.util.visible
import com.location.reminder.sound.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemindersFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    lateinit var binding: FragmentRemindersBinding
    private lateinit var adapter: TasksAdapter
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.imgSettings.setOnClickListener {
            navController.navigate(R.id.action_remindersFragment_to_settingsFragment)
        }
        binding.btnAdd.setOnClickListener {
            NewTaskFragmentDialog {
                viewModel.fetchAddedTasks()
            }.show(childFragmentManager, NewTaskFragmentDialog.TAG)
        }

        viewModel.fetchAddedTasks()
        viewModel.getAddedTasks().observe(requireActivity()) {
            if (it.isEmpty()) {
                requireContext().stopLocationUpdateService()
            } else
                requireContext().startLocationUpdateService(it as ArrayList<Task>?)

            adapter = TasksAdapter(it.reversed()) { task ->
                viewModel.deleteTask(task)
                NotificationManagerCompat.from(requireContext()).cancel(task.uid ?: 0)
            }
            if (it.isEmpty()) {
                binding.llEmpty.visible()
                binding.rvItems.gone()
            } else {
                binding.llEmpty.gone()
                binding.rvItems.visible()
                binding.rvItems.adapter = adapter
            }
        }
    }
}