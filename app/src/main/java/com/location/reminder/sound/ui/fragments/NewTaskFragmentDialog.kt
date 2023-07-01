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
import androidx.fragment.app.viewModels
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.location.reminder.sound.R
import com.location.reminder.sound.databinding.NewTaskDailogFragmentBinding
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.viewmodels.HomeViewModel
import com.location.reminder.sound.location.LocationClient
import com.location.reminder.sound.location.ViewAction
import com.location.reminder.sound.model.AddressListModel
import com.location.reminder.sound.network.PlacesApi
import com.location.reminder.sound.ui.adapters.AddressListAdapter
import com.location.reminder.sound.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class NewTaskFragmentDialog(private val onDismiss: () -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: NewTaskDailogFragmentBinding
    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var locationClient: LocationClient
    private var currentLocation: com.location.reminder.sound.model.Location? =
        com.location.reminder.sound.model.Location()

    @Inject
    lateinit var placesApi: PlacesApi

    @Inject
    lateinit var placesClient: PlacesClient

    private val addressList: ArrayList<AddressListModel> = arrayListOf()
    private lateinit var addressListAdapter: AddressListAdapter

    private var task = Task()

    companion object {
        const val TAG: String = "NewTaskFragmentDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val parentLayout =
                (it as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { layout ->
                val behaviour = BottomSheetBehavior.from(layout)
                setupFullHeight(layout)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = NewTaskDailogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClickListeners()
        setAdapter()
        initObservers()
        initTextWatchers()
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
        viewModel.getAddedTasks().observe(this) {
            requireContext().startLocationUpdateService(it as ArrayList<Task>?)
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
                    requireActivity().showConfirmationDialog(
                        task.destinationSoundMode ?: "", task.distance
                    ) {
                        saveTask()
                    }
                } else
                    saveTask()
            }
        }
        binding.imgClose.setOnClickListener {
            dismiss()
        }
        binding.txtDistanceLabel.text = resources.getString(R.string.distance_from_location, "100")
        binding.seekBarDistance.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                task.distance = (p1 + 1) * 100
                binding.txtDistanceLabel.text =
                    resources.getString(
                        R.string.distance_from_location,
                        ((p1 + 1) * 100.0).evaluateDistance()
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

        val chipGroupTo = binding.chipGroupTo.chipGroup
        for (id in chipGroupTo.checkedChipIds) {
            val chip: Chip = chipGroupTo.findViewById(id)
            task.destinationSoundMode = chip.text.toString()
        }

        //  val chipGroupFrom = binding.chipGroupFrom.chipGroup
        //  (chipGroupFrom[requireContext().checkSoundMode().second] as Chip).isChecked = true

        /*    for (id in chipGroupFrom.checkedChipIds) {
                val chip: Chip = chipGroupFrom.findViewById(id)
                task.sourceSoundMode = chip.text.toString()
            }*/
        /* binding.txtTimeIntervalLabel.text =
             resources.getString(R.string.update_location_in_every, "30")
         binding.seekBarTimeInterval.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
             override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                 task.timeInterval = (p1 + 1) * 30;
                 binding.txtTimeIntervalLabel.text = resources.getString(
                     R.string.update_location_in_every, task.timeInterval.toString()
                 )
             }

             override fun onStartTrackingTouch(p0: SeekBar?) {
             }

             override fun onStopTrackingTouch(p0: SeekBar?) {
             }
         })*/

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
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
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
            latitude = currentLocation?.lat
            longitude = currentLocation?.lng
        }
        viewModel.saveTask(task)
        viewModel.fetchAddedTasks()
        requireContext().showToast("Your task has been successfully added")
        if (!requireContext().isNotificationAccessGranted()) {
            requireActivity().showNotificationAccessPermissionDialog()
        }
        viewModel.fetchAddedTasks()
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