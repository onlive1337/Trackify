package com.onlive.trackify.ui.screens.payments.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.onlive.trackify.R
import com.onlive.trackify.data.model.PaymentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun formatPaymentStatus(status: PaymentStatus): String {
    return when (status) {
        PaymentStatus.PENDING -> stringResource(R.string.payment_status_pending)
        PaymentStatus.CONFIRMED -> stringResource(R.string.payment_status_confirmed)
        PaymentStatus.MANUAL -> stringResource(R.string.payment_status_manual)
    }
}

fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    return dateFormat.format(date)
}