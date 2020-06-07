package com.example.restopass.main.ui.settings

import com.example.restopass.R

object SettingsList {
        private val accountSettingItems = listOf(
            Setting(R.string.account),
            Setting( R.string.plan, R.drawable.ic_settings_24dp, ButtonSettingType.PLAN),
            Setting( R.string.personal_info, R.drawable.ic_person_24dp, ButtonSettingType.PERSONAL_INFO),
            Setting( R.string.payment_methods, R.drawable.ic_payment_methods_24dp, ButtonSettingType.PAYMENT_METHODS),
            Setting(R.string.booking_history, R.drawable.ic_history_24dp, ButtonSettingType.BOOKING_HISTORY)
        )

        private val otherSettingItems = listOf(
            Setting( R.string.about),
            Setting( R.string.notifications, R.drawable.ic_notifications_24dp, ButtonSettingType.NOTIFICATIONS)
        )

        val settingsItems: List<Setting> = accountSettingItems.plus(otherSettingItems)
}