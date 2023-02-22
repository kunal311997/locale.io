package com.location.reminder.sound.finalCode.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.location.reminder.sound.R
import com.location.reminder.sound.databinding.FragmentRemindersBinding
import com.location.reminder.sound.ui.fragment.NewTaskFragmentDialog


class RemindersFragment : Fragment() {

    lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtAddNew.setOnClickListener {
            NewTaskFragmentDialog().show(childFragmentManager, NewTaskFragmentDialog.TAG)
        }
    }

}