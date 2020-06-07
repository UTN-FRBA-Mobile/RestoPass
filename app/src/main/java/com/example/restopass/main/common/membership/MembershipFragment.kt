package com.example.restopass.main.common.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import com.example.restopass.common.orElse
import com.example.restopass.domain.Membership
import com.example.restopass.domain.MembershipsViewModel
import com.example.restopass.main.common.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_membership.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber


class MembershipFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var membershipAdapter: MembershipAdapter
    private lateinit var membershipsViewModel: MembershipsViewModel

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
        loader.visibility = View.VISIBLE
        coroutineScope.launch {
            try {
                membershipsViewModel.get()

                membershipAdapter.memberships = formatMembershipList(membershipsViewModel)
                membershipAdapter.notifyDataSetChanged()
                loader.visibility = View.GONE
                membershipRecycler.visibility = View.VISIBLE
            } catch (e: Exception) {
                if(isActive) {
                    Timber.e(e)
                    loader.visibility = View.GONE

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
                add(0,actualMembershipTitle)
                add(1, it.copy(isActual = true))
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