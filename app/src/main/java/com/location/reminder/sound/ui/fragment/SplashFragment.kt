package com.location.reminder.sound.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.location.reminder.sound.R
import com.location.reminder.sound.util.Constants
import com.location.reminder.sound.util.SharedPrefClient
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : Fragment() {

    @Inject
    lateinit var sharedPrefClient: SharedPrefClient
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController = Navigation.findNavController(view)

        Handler(Looper.getMainLooper()).postDelayed({
            if (sharedPrefClient.isWalkThroughDone()) openHomeFragment()
            else openWelcomeFragment()
        }, Constants.SPLASH_SCREEN_TIMEOUT)
    }

    private fun openWelcomeFragment() {
        navController.navigate(R.id.action_splashFragment_to_welcomeFragment)
    }

    private fun openHomeFragment() {
        navController.navigate(R.id.action_splashFragment_to_homeFragment)
    }
}
