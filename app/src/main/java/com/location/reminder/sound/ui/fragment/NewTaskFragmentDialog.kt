package com.location.reminder.sound.ui.fragment

import android.Manifest
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.location.reminder.sound.R
import com.location.reminder.sound.databinding.NewTaskDailogFragmentBinding
import com.location.reminder.sound.finalCode.viewmodels.HomeViewModel
import com.location.reminder.sound.location.LocationClient
import com.location.reminder.sound.location.ViewAction
import com.location.reminder.sound.util.checkSoundMode
import com.location.reminder.sound.util.getCompleteAddressString
import com.location.reminder.sound.util.isPermissionGranted
import com.location.reminder.sound.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NewTaskFragmentDialog : BottomSheetDialogFragment() {

    private lateinit var binding: NewTaskDailogFragmentBinding
    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var locationClient: LocationClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = NewTaskDailogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClickListeners()
        initObservers()
    }

    private fun initObservers() {
        locationClient.getViewAction().observe(requireActivity()) { action ->
            when (action) {
                is ViewAction.OnLocationFetched -> {
                    action.location?.let { location -> onLocationFetched(location) }
                }
                is ViewAction.OnError -> {
                    requireActivity().showToast(action.error?.message.toString())
                }
                else -> {

                }
            }
        }
    }

    private fun onLocationFetched(location: Location) {
        val completeAddress =
            requireActivity().getCompleteAddressString(location.latitude, location.longitude)
        Log.e(TAG, "Current Address : $completeAddress")
        binding.edtAddress.setText(completeAddress)
    }

    private fun initOnClickListeners() {
        val currentSoundMode = requireContext().checkSoundMode().second
        (binding.chipGroupFrom[currentSoundMode] as Chip).isChecked = true
        binding.seekBarDistance.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.txtDistanceLabel.text = resources.getString(R.string.distance_from_location, (p1 * 100).toString())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        val timeIntervalList = arrayListOf(30, 60, 90, 120, 150)
        binding.seekBarTimeInterval.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.txtTimeIntervalLabel.text = resources.getString(R.string.update_location_in_every, (timeIntervalList[p1]).toString())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        binding.llUseMyCurrentLocation.setOnClickListener {
            checkLocationPermissions()
        }
    }

    private fun checkLocationPermissions() {
        when {
            requireActivity().isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    requireActivity().isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                fetchLastLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                requireActivity().showToast("The app requires this feature.")
                showLocationPermissionDialog()
            }

            else -> requestLocationPermissions()
        }
    }

    private fun fetchLastLocation() {
        requireActivity().showToast("Fetching your location...")
        locationClient.fetchLastLocation()
    }

    private fun showLocationPermissionDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dailog_location_permission)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtAllow = dialog.findViewById<TextView>(R.id.txtAllow)
        val txtDeny = dialog.findViewById<TextView>(R.id.txtDeny)

        txtAllow.setOnClickListener {
            requestLocationPermissions()
            dialog.dismiss()
        }
        txtDeny.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun requestLocationPermissions() {
        locationRequestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private val locationRequestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (requireActivity().isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
            requireActivity().isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            fetchLastLocation()
        } else {
            requireActivity().showToast("Please allow location permissions from Settings.")
        }

    }

    companion object {
        const val TAG: String = "NewTaskFragmentDialog"
    }
}