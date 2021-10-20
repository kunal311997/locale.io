package com.location.reminder.sound.ui

import android.app.Dialog
import android.app.NotificationManager
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.SphericalUtil
import com.location.reminder.sound.*
import com.location.reminder.sound.location.LocationClientUtil
import com.location.reminder.sound.location.MyBroadcastReceiver
import com.location.reminder.sound.model.AddressListModel
import com.location.reminder.sound.model.LocationData
import com.location.reminder.sound.model.PlacesDetailsResponse
import com.location.reminder.sound.network.MyApi
import com.location.reminder.sound.network.RetrofitUtil
import com.location.reminder.sound.util.Constants
import com.location.reminder.sound.util.SharedPrefClient
import kotlinx.android.synthetic.main.activity_add_reminder.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt


class AddReminderActivity : AppCompatActivity(), LocationClientUtil.LocationClientUtilListener,
    AddressListAdapter.AddressClickListener {

    private var lastSoundMode: String = ""
    private val TAG = "AddReminderActivity"
    private lateinit var addressListAdapter: AddressListAdapter
    private lateinit var sharedPrefClient: SharedPrefClient
    private lateinit var placesClient: PlacesClient
    private val addressList: ArrayList<AddressListModel> = arrayListOf()
    private var location: LocationData? = null
    private var myApi: MyApi? = null
    private var selectedSoundMode = ""
    private var selectedDistance = 100
    private var selectedTime = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        initSharedPrefClient()
        initializeGooglePlacesApi()
        initRetrofit()
        addOnClickListeners()
        setAdapter()
        addTextWatchers()
        setDataToUI()
        checkIntentData()
    }

    private fun checkIntentData() {
        when {
            intent.getBooleanExtra(Constants.IS_FROM_RECEIVER, false) -> {
                getDataFromPref(true)
            }
            intent.getBooleanExtra(Constants.IS_FROM_EDIT, false) -> {
                getDataFromPref(false)
            }
            else -> {
                checkLocationPermissions()
            }
        }
    }

    private fun initSharedPrefClient() {
        sharedPrefClient = SharedPrefClient()
        sharedPrefClient.init(this)
    }

    private fun initRetrofit() {
        myApi = RetrofitUtil.getInstance()?.create(MyApi::class.java)
    }

    private fun initializeGooglePlacesApi() {
        if (!Places.isInitialized()) {
            val gApiKey: String = BuildConfig.API_KEY
            Places.initialize(this, gApiKey)
        }
        placesClient = Places.createClient(this)
    }

    private fun addTextWatchers() {
        edtSearchAddress.addTextChangedListener {
            addressList.clear()
            if (it?.length ?: 0 > 2) {
                rvAddresses.visible()
                fetchPlaces(it.toString())
            } else {
                rvAddresses.gone()
                addressListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setAdapter() {
        addressListAdapter = AddressListAdapter(addressList, this)
        rvAddresses.adapter = addressListAdapter
    }

    private fun checkLocationPermissions() {
        when {
            isPermissionGranted(android.Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                fetchLastLocation()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                Log.e(TAG, "getCurrentLocation: shouldShowRequestPermissionRationale")
                this.showToast("The app requires this feature")
                showLocationPermissionDialog()
            }

            else -> {
                showLocationPermissionDialog()
            }
        }
    }

    private fun showLocationPermissionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dailog_location_permission)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtAllow = dialog.findViewById<TextView>(R.id.txtAllow)
        val txtDeny = dialog.findViewById<TextView>(R.id.txtDeny)

        txtAllow.setOnClickListener {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), Constants.LOCATION_PERMISSIONS_REQUEST_CODE
            )
            dialog.dismiss()
        }
        txtDeny.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showNotificationAccessPermissionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dailog_notification_access)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtOk = dialog.findViewById<TextView>(R.id.txtOk)
        val txtCancel = dialog.findViewById<TextView>(R.id.txtCancel)

        txtOk.setOnClickListener {
            saveDataToPref()
            openDoNotDisturbSettings()
            dialog.dismiss()
        }
        txtCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openDoNotDisturbSettings() {
        val batteryChangeReceiver = MyBroadcastReceiver()
        registerReceiver(
            batteryChangeReceiver, IntentFilter(
                NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED
            )
        )
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun addOnClickListeners() {
        imageView2.setOnClickListener {
            checkLocationPermissions()
        }

        textView.setOnClickListener {
            checkLocationPermissions()
        }

        btnDone.setOnClickListener {
            checkDataAndProceed()
        }

        txtTime.setOnClickListener {
            openTimeIntervalList()
        }

        txtDistance.setOnClickListener {
            openDistanceIntervalList()
        }

        txtSound.setOnClickListener {
            changeSoundColor()
        }

        txtVibrate.setOnClickListener {
            changeVibrateColor()
        }

        txtMute.setOnClickListener {
            changeMuteColor()
        }
    }

    private fun changeMuteColor() {
        txtSound.background = ContextCompat.getDrawable(this, R.drawable.bg_unselected)
        txtVibrate.background = ContextCompat.getDrawable(this, R.drawable.bg_unselected)
        txtMute.background = ContextCompat.getDrawable(this, R.drawable.bg_selected)
        txtSound.setTextColor(ContextCompat.getColor(this, R.color.black))
        txtVibrate.setTextColor(ContextCompat.getColor(this, R.color.black))
        txtMute.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedSoundMode = resources.getString(R.string.mute)
        setTextViewDrawableColor(txtSound, R.color.black)
        setTextViewDrawableColor(txtVibrate, R.color.black)
        setTextViewDrawableColor(txtMute, R.color.white)
    }

    private fun changeVibrateColor() {
        txtSound.background = ContextCompat.getDrawable(this, R.drawable.bg_unselected)
        txtVibrate.background = ContextCompat.getDrawable(this, R.drawable.bg_selected)
        txtMute.background = ContextCompat.getDrawable(this, R.drawable.bg_unselected)
        txtSound.setTextColor(ContextCompat.getColor(this, R.color.black))
        txtVibrate.setTextColor(ContextCompat.getColor(this, R.color.white))
        txtMute.setTextColor(ContextCompat.getColor(this, R.color.black))
        selectedSoundMode = resources.getString(R.string.vibrate)
        setTextViewDrawableColor(txtSound, R.color.black)
        setTextViewDrawableColor(txtVibrate, R.color.white)
        setTextViewDrawableColor(txtMute, R.color.black)
    }

    private fun changeSoundColor() {
        txtSound.background = ContextCompat.getDrawable(this, R.drawable.bg_selected)
        txtVibrate.background = ContextCompat.getDrawable(this, R.drawable.bg_unselected)
        txtMute.background = ContextCompat.getDrawable(this, R.drawable.bg_unselected)
        txtSound.setTextColor(ContextCompat.getColor(this, R.color.white))
        txtVibrate.setTextColor(ContextCompat.getColor(this, R.color.black))
        txtMute.setTextColor(ContextCompat.getColor(this, R.color.black))
        selectedSoundMode = resources.getString(R.string.sound)
        setTextViewDrawableColor(txtSound, R.color.white)
        setTextViewDrawableColor(txtVibrate, R.color.black)
        setTextViewDrawableColor(txtMute, R.color.black)
    }

    private fun checkDataAndProceed() {
        if (isNotificationAccessGranted()) {
            if (checkValidations()) {
                saveDataToPref()
                startLocationUpdateService()
                this.showToast("Your task has been successfully added")
                setResult(RESULT_OK)
                finish()
            }
        } else {
            showNotificationAccessPermissionDialog()
        }
    }

    private fun saveDataToPref() {
        sharedPrefClient.setAddress(txtLocation.text.toString())
        sharedPrefClient.setCreatedAt(getCurrentDate())
        sharedPrefClient.setSoundMode(selectedSoundMode)
        sharedPrefClient.setLatitude(location?.latitude ?: 0.0)
        sharedPrefClient.setLongitude(location?.longitude ?: 0.0)
        sharedPrefClient.setUpdateTime(selectedTime)
        sharedPrefClient.setDistance(selectedDistance)
        sharedPrefClient.setLastSoundMode(lastSoundMode)
        sharedPrefClient.setServiceRunning(true)
    }

    private fun getDataFromPref(isFromReceiver: Boolean) {
        when (sharedPrefClient.getSoundMode()) {
            resources.getString(R.string.sound) -> changeSoundColor()
            resources.getString(R.string.vibrate) -> changeVibrateColor()
            resources.getString(R.string.mute) -> changeMuteColor()
        }
        location = LocationData(sharedPrefClient.getLatitude(), sharedPrefClient.getLongitude())
        txtLocation.text = sharedPrefClient.getAddress()

        txtDistance.text = sharedPrefClient.getDistance().toString()
        selectedDistance = sharedPrefClient.getDistance()

        txtTime.text = sharedPrefClient.getUpdateTime().toString()
        selectedTime = sharedPrefClient.getUpdateTime()
        if (isFromReceiver)
            checkDataAndProceed()
    }

    private fun checkValidations(): Boolean {
        if (location?.latitude == null && location?.longitude == null) {
            showToast("Location in not selected")
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.LOCATION_PERMISSIONS_REQUEST_CODE -> {
                if (isPermissionGranted(android.Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)
                ) {
                    fetchLastLocation()
                } else {
                    this.showToast("Please allow location permissions from Settings.")
                }
                return
            }
        }
    }

    private fun fetchLastLocation() {
        showToast("Fetching location.....")
        val locationClientUtil = LocationClientUtil(this, 10, this)
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            locationClientUtil.removeLocationUpdates()
        }
    }

    override fun onLocationFetched(location: LocationData) {
        val completeAddressString =
            this.getCompleteAddressString(location.latitude ?: 0.0, location.longitude ?: 0.0)
        Log.e(TAG, " latitude ${location.latitude} longitude ${location.longitude}")
        Log.e(TAG, "onLocationFetched: $completeAddressString")
        this.location = location
        txtLocation.text = completeAddressString
    }

    override fun onError() {
        Log.e(TAG, "onError: ")
    }

    private fun fetchPlaces(searchText: String) {
        val latLng = LatLng(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
        val token = AutocompleteSessionToken.newInstance()
        val latLngBounds = toBounds(latLng)
        val bounds = RectangularBounds.newInstance(latLngBounds)
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setOrigin(latLng)
            .setLocationBias(bounds)
            .setQuery(searchText)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                Log.i(
                    "", "number of results in search places response"
                            + response.autocompletePredictions.size
                )
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
                    Log.e(TAG, "onCreate: " + prediction.placeId)
                }
                addressListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception: Exception -> exception.printStackTrace() }
    }

    override fun onItemClick(position: Int) {
        val completeAddressString = addressList[position].placeName
        txtLocation.text = completeAddressString
        rvAddresses.gone()
        callGetPlaceDetailsApi(addressList[position].placeId)
    }

    private fun openTimeIntervalList() {
        val status: MutableList<Int> = ArrayList()
        status.add(5)
        status.add(10)
        status.add(20)
        status.add(30)
        status.add(40)
        status.add(50)
        status.add(60)
        val statusPopupList = ListPopupWindow(this)
        val adapter: ArrayAdapter<*> =
            ArrayAdapter(this, R.layout.item_simple_status, R.id.txtItem, status)
        statusPopupList.anchorView = txtTime
        statusPopupList.setAdapter(adapter)
        statusPopupList.setOnItemClickListener { parent, view, position, id ->
            val item = status[position]
            txtTime.text = item.toString()
            selectedTime = item
            setDataToUI()
            statusPopupList.dismiss()
        }
        statusPopupList.show()
    }

    private fun openDistanceIntervalList() {
        val status: MutableList<Int> = ArrayList()
        status.add(100)
        status.add(150)
        status.add(200)
        status.add(250)
        status.add(300)
        status.add(350)
        status.add(400)
        val statusPopupList = ListPopupWindow(this)
        val adapter: ArrayAdapter<*> =
            ArrayAdapter(this, R.layout.item_simple_status, R.id.txtItem, status)
        statusPopupList.anchorView = txtDistance
        statusPopupList.setAdapter(adapter)
        statusPopupList.setOnItemClickListener { parent, view, position, id ->
            val item = status[position]
            txtDistance.text = item.toString()
            selectedDistance = item
            setDataToUI()
            statusPopupList.dismiss()
        }
        statusPopupList.show()
    }

    private fun callGetPlaceDetailsApi(placeId: String?) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val response: PlacesDetailsResponse? = myApi?.placesDetailAPI(
                    Constants.GOOGLE_DETAIL_URL, placeId ?: "", BuildConfig.API_KEY
                )
                Log.e(TAG, "getPlaceDetails: $response")
                response?.result?.geometry?.location?.let {
                    location?.latitude = it.lat
                    location?.longitude = it.lng
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "getPlaceDetails: $e")
        }
    }

    private fun setDataToUI() {
        lastSoundMode = checkSoundMode()
        txtCurrentSoundMode.text =
            resources.getString(R.string.your_device_is_currently_on_x_mode, lastSoundMode)
        txtNotificationIfo.text = resources.getString(
            R.string._1_you_will_be_notified_whenever_the_selected_location_is_within_100_metres_n,
            selectedDistance.toString(), selectedTime.toString()
        )
        txtDistance.text = selectedDistance.toString()
        txtTime.text = selectedTime.toString()
    }

}