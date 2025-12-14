package com.app.e_commerce.Enum;

/**
 * Enum representing the various states of an invoice in the system.
 * 
 * DRAFT - Invoice is being created/edited, fully editable
 * PENDING - Invoice submitted for review, limited editing
 * FINALIZED - Invoice is locked and ready for export, no editing allowed
 * CANCELLED - Invoice has been voided/cancelled
 */
public enum InvoiceStatus {
    DRAFT,      // Editable by users
    PENDING,    // Submitted for review
    FINALIZED,  // Locked, ready for export
    CANCELLED   // Voided invoice
}
