package com.location.reminder.sound.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.location.reminder.sound.R
import com.location.reminder.sound.databinding.FragmentSettingsBinding
import com.location.reminder.sound.domain.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.seekBarTimeInterval.progress = (viewModel.getUpdateTime() / 15) - 1
        binding.imgBack.setOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }
        binding.txtTimeIntervalLabel.text =
            resources.getString(
                R.string.update_location_in_every,
                viewModel.getUpdateTime().toString()
            )
        binding.seekBarTimeInterval.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val timeInterval = (p1 + 1) * 15
                binding.txtTimeIntervalLabel.text = resources.getString(
                    R.string.update_location_in_every, timeInterval.toString()
                )
                viewModel.setUpdateTime(timeInterval)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
    }

}
