package com.location.reminder.sound.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import com.google.android.material.tabs.TabLayoutMediator
import com.location.reminder.sound.databinding.FragmentHomeBinding
import com.location.reminder.sound.ui.adapters.FragmentAdapter
import com.location.reminder.sound.viewmodels.HomeViewModel
import com.location.reminder.sound.util.Constants.tabList
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var navController: NavController
    private val viewModel: HomeViewModel by viewModels()
    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewPager.adapter = FragmentAdapter(requireActivity(), tabList)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList[position].first
        }.attach()

        initSharedPref()
        setDataToUI()
        addOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        checkSharedPrefData()
    }

    private fun checkSharedPrefData() {
        /*  if (sharedPrefClient.getAddress() == "") {
              groupNoDataPresent.visible()
              clDataPresent.gone()
          } else {
              groupNoDataPresent.gone()
              clDataPresent.visible()
          }*/
    }

    private fun initSharedPref() {
        /*sharedPrefClient = SharedPrefClient()
        sharedPrefClient.init(requireContext())*/
    }

    private fun addOnClickListeners() {
        /* btnAddReminder.setOnClickListener {
             *//*val intent = Intent(requireContext(), AddReminderActivity::class.java)
            resultLauncher.launch(intent)*//*
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
        }*/
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                /* groupNoDataPresent.gone()
                 clDataPresent.visible()*/
                setDataToUI()
            }
        }

    private fun setDataToUI() {
        /* txtSoundMode.text = sharedPrefClient.getSoundMode()
         txtLocationLabel.text = resources.getString(
             R.string.whenever_you_are_within_x_meters_of, sharedPrefClient.getDistance().toString()
         )
         txtLocationUpdationTime.text = resources.getString(
             R.string.location_will_update_every_x_seconds,
             sharedPrefClient.getUpdateTime().toString()
         )
         txtLocation.text = sharedPrefClient.getAddress()
         textCreatedAt.text = sharedPrefClient.getCreatedAt()*/
    }
}