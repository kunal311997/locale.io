package com.location.reminder.sound.finalCode.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.location.reminder.sound.R
import com.location.reminder.sound.databinding.FragmentWelcomeBinding
import com.location.reminder.sound.finalCode.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var binding: FragmentWelcomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        if (viewModel.isWalkThroughCompleted()) {
            openHomeFragment()
        }
        binding.btnContinue.setOnClickListener {
            viewModel.completeWalkThrough()
            navController.navigate(R.id.action_welcomeFragment_to_homeFragment)
        }
    }

    private fun openHomeFragment() {
        navController.navigate(R.id.action_welcomeFragment_to_homeFragment)
    }
}