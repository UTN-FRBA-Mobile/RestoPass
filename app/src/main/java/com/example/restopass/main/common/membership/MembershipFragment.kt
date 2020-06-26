package com.example.restopass.main.common.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import com.example.restopass.common.AppPreferences
import com.example.restopass.domain.Membership
import com.example.restopass.domain.MembershipsViewModel
import com.example.restopass.domain.SelectedMembershipViewModel
import com.example.restopass.main.common.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_membership.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber


class MembershipFragment : Fragment(), MembershipAdapterListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var membershipAdapter: MembershipAdapter
    private lateinit var membershipsViewModel: MembershipsViewModel

    private lateinit var selectedMembership: SelectedMembershipViewModel

    val job = Job()
    val coroutineScope = CoroutineScope(job + Main)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_membership, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        membershipsViewModel = ViewModelProvider(requireActivity()).get(MembershipsViewModel::class.java)

        membershipAdapter = MembershipAdapter(this)
        recyclerView = membershipRecycler.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = membershipAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        notEnrolledLoader.visibility = View.VISIBLE
        coroutineScope.launch {
            try {
                membershipsViewModel.get()

                membershipAdapter.memberships = formatMembershipList(membershipsViewModel)
                membershipAdapter.notifyDataSetChanged()
                notEnrolledLoader.visibility = View.GONE
                membershipRecycler.visibility = View.VISIBLE
            } catch (e: Exception) {
                if(isActive) {
                    Timber.e(e)
                    notEnrolledLoader.visibility = View.GONE

                    val titleView: View =
                        layoutInflater.inflate(R.layout.alert_dialog_title, container, false)
                    AlertDialog.getAlertDialog(
                        context,
                        titleView,
                        view
                    ).show()
                }
            }
        }
    }

    override fun onEnrollClick(membership: Membership) {
        membershipRecycler.visibility = View.GONE
        notEnrolledLoader.visibility = View.VISIBLE
        coroutineScope.launch {
            try {
                membershipsViewModel.update(membership)

                membershipAdapter.memberships = formatMembershipList(membershipsViewModel)
                membershipAdapter.notifyDataSetChanged()

                AppPreferences.user.apply {
                    AppPreferences.user = this.copy(actualMembership = membership.membershipId)
                }

                membershipsViewModel.wasEnrolled = true
                findNavController().navigate(R.id.navigation_enrolled_home)
            } catch (e: Exception) {
                if(isActive) {
                    Timber.e(e)
                    notEnrolledLoader.visibility = View.GONE

                    val titleView: View =
                        layoutInflater.inflate(R.layout.alert_dialog_title, container, false)
                    AlertDialog.getAlertDialog(
                        context,
                        titleView,
                        view
                    ).show()
                }
            }
        }
    }


    override fun onDetailsClick(membership: Membership) {
        selectedMembership = ViewModelProvider(requireActivity()).get(SelectedMembershipViewModel::class.java)
        selectedMembership.membership = membership
    }

    private fun formatMembershipList(response: MembershipsViewModel): List<Membership> {
        val actualMembershipTitle =
            Membership(
                name = "Tu Membresía",
                isTitle = true
            )
        val otherMembershipsTitle =
            Membership(
                name = "Otras Membresías",
                isTitle = true
            )
        val membershipList = response.memberships.toMutableList()
        membershipList.apply {
            response.actualMembership?.let {
                it.isActual = true
                add(0,actualMembershipTitle)
                add(1, it)
                add(2, otherMembershipsTitle)
            }
        }
        return membershipList
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()

    }

}