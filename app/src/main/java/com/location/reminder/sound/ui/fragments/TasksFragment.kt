package com.location.reminder.sound.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.location.reminder.sound.R
import com.location.reminder.sound.databinding.FragmentTasksBinding
import com.location.reminder.sound.domain.viewmodels.HomeViewModel
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.ui.adapters.TasksAdapter
import com.location.reminder.sound.util.gone
import com.location.reminder.sound.util.showDeleteConfirmationDialog
import com.location.reminder.sound.util.startLocationUpdateService
import com.location.reminder.sound.util.stopLocationUpdateService
import com.location.reminder.sound.util.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment : Fragment() {
    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var binding: FragmentTasksBinding
    private lateinit var adapter: TasksAdapter
    private lateinit var navController: NavController
    private val tasks: ArrayList<Task> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.lifecycleOwner = this
        viewModel.fetchAddedTasks()

        initAdapter()
        initOnClickListeners()
        initObservers()
    }

    private fun initObservers() {
        viewModel.getAddedTasks().observe(viewLifecycleOwner) {
            tasks.clear()
            tasks.addAll(it)
            adapter.notifyDataSetChanged()
            if (it.isEmpty()) {
                requireContext().stopLocationUpdateService()
            } else requireContext().startLocationUpdateService(tasks, viewModel.getUpdateTime())

            if (it.isEmpty()) {
                binding.llEmpty.visible()
                binding.txtStartAdding.visible()
                binding.rvItems.gone()
            } else {
                binding.llEmpty.gone()
                binding.txtStartAdding.gone()
                binding.rvItems.visible()
            }
        }
    }

    private fun initOnClickListeners() {
        binding.imgSettings.setOnClickListener {
            navController.navigate(R.id.action_remindersFragment_to_settingsFragment)
        }
        binding.btnAdd.setOnClickListener {
            CreateTaskFragmentDialog {
            }.show(childFragmentManager, CreateTaskFragmentDialog.TAG)
        }
    }

    private fun initAdapter() {
        adapter = TasksAdapter(
            tasks,
            { task ->
                CreateTaskFragmentDialog(task) {
                    viewModel.fetchAddedTasks()
                }.show(childFragmentManager, CreateTaskFragmentDialog.TAG)
            },
            { task ->
                requireActivity().showDeleteConfirmationDialog {
                    viewModel.deleteTask(task)
                    NotificationManagerCompat.from(requireContext()).cancel(task.uid)
                }
            })
        binding.rvItems.adapter = adapter
    }
}