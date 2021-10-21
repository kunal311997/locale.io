package com.location.reminder.sound.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.location.reminder.sound.R
import com.location.reminder.sound.util.SharedPrefClient
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : Fragment() {

    private lateinit var sharedPrefClient: SharedPrefClient
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

      /*  sharedPrefClient = SharedPrefClient()
        sharedPrefClient.init(requireContext())*/
        navController = Navigation.findNavController(view)

        btnContinue.setOnClickListener {
  //          sharedPrefClient.updateWalkThroughDone(true)
            navController.navigate(R.id.action_welcomeFragment_to_homeFragment)
        }
    }
}