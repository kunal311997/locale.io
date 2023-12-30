package com.location.reminder.sound.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.location.reminder.sound.R
import com.location.reminder.sound.databinding.CreateTaskDailogFragmentBinding
import com.location.reminder.sound.location.LocationClient
import com.location.reminder.sound.location.ViewAction
import com.location.reminder.sound.domain.viewmodels.HomeViewModel
import com.location.reminder.sound.model.AddressListModel
import com.location.reminder.sound.model.Location
import com.location.reminder.sound.model.SoundMode
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.network.PlacesApi
import com.location.reminder.sound.ui.adapters.AddressListAdapter
import com.location.reminder.sound.util.callGetPlaceDetailsApi
import com.location.reminder.sound.util.checkSoundMode
import com.location.reminder.sound.util.evaluateDistance
import com.location.reminder.sound.util.fetchPlaces
import com.location.reminder.sound.util.getCompleteAddressString
import com.location.reminder.sound.util.gone
import com.location.reminder.sound.util.isLocationPermissionsGranted
import com.location.reminder.sound.util.isNotificationAccessGranted
import com.location.reminder.sound.util.showConfirmationDialog
import com.location.reminder.sound.util.showLocationPermissionDialog
import com.location.reminder.sound.util.showNotificationAccessPermissionDialog
import com.location.reminder.sound.util.showToast
import com.location.reminder.sound.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CreateTaskFragmentDialog(val task: Task = Task(), private val onDismiss: () -> Unit) :
    BottomSheetDialogFragment() {

    private lateinit var binding: CreateTaskDailogFragmentBinding
    private val viewModel: HomeViewModel by activityViewModels()

    @Inject
    lateinit var locationClient: LocationClient
    private var currentLocation: Location? = Location()

    @Inject
    lateinit var placesApi: PlacesApi

    @Inject
    lateinit var placesClient: PlacesClient

    private val addressList: ArrayList<AddressListModel> = arrayListOf()
    private lateinit var addressListAdapter: AddressListAdapter

    companion object {
        const val TAG: String = "NewTaskFragmentDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireActivity(), theme)
        dialog.setOnShowListener {
            val parentLayout =
                (it as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { layout ->
                val behaviour = BottomSheetBehavior.from(layout)
                val layoutParams = layout.layoutParams
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
                layout.layoutParams = layoutParams
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = CreateTaskDailogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDataForEditMode()
        initOnClickListeners()
        setAdapter()
        initObservers()
        initTextWatchers()
    }

    private fun setDataForEditMode() {
        binding.edtTitle.setText(task.title)
        binding.edtDescription.setText(task.description)
        binding.edtAddress.setText(task.address)
        currentLocation?.lat = task.latitude
        currentLocation?.lng = task.longitude
        if (task.destinationSoundMode != null) {
            when (task.destinationSoundMode) {
                SoundMode.RINGER -> binding.chipGroupTo.chipGroup.check(R.id.ringer)
                SoundMode.VIBRATE -> binding.chipGroupTo.chipGroup.check(R.id.vibrate)
                SoundMode.SILENT -> binding.chipGroupTo.chipGroup.check(R.id.silent)
                else -> {}
            }
            binding.checkboxSound.isChecked = true
            binding.llChips.visible()
        }
    }

    private fun initObservers() {
        locationClient.getViewAction().observe(requireActivity()) { action ->
            when (action) {
                is ViewAction.OnLocationFetched -> {
                    action.location?.let { location ->
                        currentLocation?.lng = location.longitude
                        currentLocation?.lat = location.latitude
                        binding.edtAddress.setText(
                            requireActivity().getCompleteAddressString(
                                location.latitude, location.longitude
                            ).trim()
                        )
                    }
                }

                is ViewAction.OnError -> {
                    requireActivity().showToast(action.error?.message.toString())
                }
            }
        }
    }

    private fun setAdapter() {
        addressListAdapter = AddressListAdapter(addressList) { position ->
            // On item click
            binding.edtAddress.setText(addressList[position].placeName)
            binding.rvAddresses.gone()
            CoroutineScope(Dispatchers.IO).launch {

            }
            CoroutineScope(Dispatchers.IO).launch {
                val location = placesApi.callGetPlaceDetailsApi(addressList[position].placeId)
                currentLocation?.lng = location?.lng
                currentLocation?.lat = location?.lat
            }
        }
        binding.rvAddresses.adapter = addressListAdapter
    }

    private fun initTextWatchers() {
        binding.searchView.requestFocus()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?) = true

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(p0: String?): Boolean {
                addressList.clear()
                if ((p0?.length ?: 0) > 2) {
                    binding.rvAddresses.visible()
                    placesClient.fetchPlaces(p0.toString(), currentLocation) { response ->
                        val sb = StringBuilder()
                        for (prediction in response.autocompletePredictions) {
                            sb.append(prediction.getPrimaryText(null).toString())
                            sb.append("\n")
                            addressList.add(
                                AddressListModel(
                                    prediction.placeId,
                                    prediction.getPrimaryText(null).toString(),
                                    prediction.getSecondaryText(null).toString()
                                )
                            )
                        }
                        addressListAdapter.notifyDataSetChanged()
                    }
                } else {
                    binding.rvAddresses.gone()
                    addressListAdapter.notifyDataSetChanged()
                }
                return true
            }

        })

    }

    private fun initOnClickListeners() {

        binding.llUseMyCurrentLocation.setOnClickListener { checkLocationPermissions() }

        binding.edtAddress.setOnClickListener {
            requireContext().showToast("Search address or Use my current location")
        }
        binding.btnSave.setOnClickListener {
            if (checkValidations()) {
                if (binding.checkboxSound.isChecked) {
                    task.destinationSoundMode =
                        when (binding.chipGroupTo.chipGroup.checkedChipId) {
                            R.id.ringer -> SoundMode.RINGER
                            R.id.vibrate -> SoundMode.VIBRATE
                            R.id.silent -> SoundMode.SILENT
                            else -> null
                        }
                    task.destinationSoundMode?.name?.let { it1 ->
                        requireActivity().showConfirmationDialog(
                            it1, task.distance
                        ) {
                            saveTask()
                        }
                    }
                } else saveTask()
            }
        }
        binding.imgClose.setOnClickListener {
            dismiss()
        }
        binding.txtDistanceLabel.text =
            resources.getString(R.string.distance_from_location, "100 metres")
        binding.seekBarDistance.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                task.distance = (p1 + 1) * 100
                binding.txtDistanceLabel.text = resources.getString(
                    R.string.distance_from_location, ((p1 + 1) * 100.0).evaluateDistance()
                )
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        binding.checkboxSound.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.llChips.visible()
            else {
                binding.llChips.gone()
                task.destinationSoundMode = null
            }
        }
    }

    private fun checkLocationPermissions() = when {
        requireActivity().isLocationPermissionsGranted() -> fetchLastLocation()

        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
            requireActivity().showToast("The app requires location permissions.")
            requireContext().showLocationPermissionDialog {
                requestLocationPermissions()
            }
        }

        else -> requestLocationPermissions()
    }

    private fun fetchLastLocation() {
        requireActivity().showToast("Fetching your location...")
        locationClient.fetchLastLocation()
    }


    private fun requestLocationPermissions() {
        locationRequestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
    }

    private val locationRequestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (requireActivity().isLocationPermissionsGranted()) {
            fetchLastLocation()
        } else {
            requireActivity().showToast("Please allow location permissions from Settings.")
        }
    }

    private fun saveTask() {
        task.apply {
            title = binding.edtTitle.text.toString()
            description = binding.edtDescription.text.toString()
            address = binding.edtAddress.text.toString()
            latitude = currentLocation?.lat ?: 0.0
            longitude = currentLocation?.lng ?: 0.0
            sourceSoundMode = requireActivity().checkSoundMode()
        }
        if (task.uid == 0) {
            viewModel.saveTask(task)
            requireContext().showToast("Your task has been successfully added")
        } else {
            viewModel.updateTask(task)
            requireContext().showToast("Your task has been successfully updated")
        }

        if (!requireActivity().isNotificationAccessGranted()) {
            requireActivity().showNotificationAccessPermissionDialog()
        }
        onDismiss.invoke()
        dismiss()
    }

    private fun checkValidations(): Boolean {
        if (currentLocation?.lat == null && currentLocation?.lng == null) {
            requireContext().showToast("Location is not selected")
            return false
        }
        if (binding.checkboxSound.isChecked && binding.chipGroupTo.chipGroup.checkedChipIds.isEmpty()) {
            requireContext().showToast("Please select sound mode")
            return false
        }
        return true
    }
}