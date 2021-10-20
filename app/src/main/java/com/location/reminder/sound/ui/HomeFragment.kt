package com.location.reminder.sound.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.location.reminder.sound.*
import com.location.reminder.sound.util.Constants
import com.location.reminder.sound.util.SharedPrefClient
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            /*param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

/*    companion object {
        */
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     *//*
         @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    *//*putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)*//*
                }
            }
    }*/

    private lateinit var sharedPrefClient: SharedPrefClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSharedPref()
        setDataToUI()
        addOnClickListeners()
        navController = Navigation.findNavController(view)
     }

    override fun onResume() {
        super.onResume()
        checkSharedPrefData()
    }

    private fun checkSharedPrefData() {
        if (sharedPrefClient.getAddress() == "") {
            groupNoDataPresent.visible()
            clDataPresent.gone()
        } else {
            groupNoDataPresent.gone()
            clDataPresent.visible()
        }
    }

    private fun initSharedPref() {
        sharedPrefClient = SharedPrefClient()
        sharedPrefClient.init(requireContext())
    }

    private fun addOnClickListeners() {
        btnAddReminder.setOnClickListener {
            /*val intent = Intent(requireContext(), AddReminderActivity::class.java)
            resultLauncher.launch(intent)*/
            navController.navigate(R.id.action_homeFragment_to_addTaskFragment)
        }

        imgEdit.setOnClickListener {
            val intent = Intent(requireContext(), AddReminderActivity::class.java)
            intent.putExtra(Constants.IS_FROM_EDIT, true)
            resultLauncher.launch(intent)
        }

        imgPlayPause.setOnClickListener {
            if (sharedPrefClient.isServiceRunning()) {
                imgPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                requireActivity().stopLocationUpdateService()
                requireActivity().showToast("Stopped")
                sharedPrefClient.setServiceRunning(false)
            } else {
                imgPlayPause.setImageResource(R.drawable.ic_baseline_stop_circle_24)
                requireActivity().startLocationUpdateService()
                requireActivity().showToast("Started")
                sharedPrefClient.setServiceRunning(true)
            }
        }

        imgDelete.setOnClickListener {
            sharedPrefClient.clearData()
            groupNoDataPresent.visible()
            clDataPresent.gone()
            requireActivity().stopLocationUpdateService()
            NotificationManagerCompat.from(requireActivity()).cancelAll()
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                groupNoDataPresent.gone()
                clDataPresent.visible()
                setDataToUI()
            }
        }

    private fun setDataToUI() {
        txtSoundMode.text = sharedPrefClient.getSoundMode()
        txtLocationLabel.text = resources.getString(
            R.string.whenever_you_are_within_x_meters_of, sharedPrefClient.getDistance().toString()
        )
        txtLocationUpdationTime.text = resources.getString(
            R.string.location_will_update_every_x_seconds,
            sharedPrefClient.getUpdateTime().toString()
        )
        txtLocation.text = sharedPrefClient.getAddress()
        textCreatedAt.text = sharedPrefClient.getCreatedAt()
    }
}