package org.example.iosfirebasehope.navigation.events

interface AllCustomersScreenEvent {
    data object OnBackClick : AllCustomersScreenEvent
    data class OnCustomerClick(val gasList: List<String>, val customerDetails: String, val cylinderDetails : List<Map<String, String>>) : AllCustomersScreenEvent
}