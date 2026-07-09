package com.karigar.app.data

data class LegalDoc(val key: String, val title: String, val body: String)

object Legal {

    val TERMS = LegalDoc(
        "terms",
        "Terms & Conditions of Use",
        """Effective Date: July 6, 2026

Welcome to Karigar. These Terms & Conditions ("Terms") govern your access to and use of the Karigar mobile application, website, and related services (collectively, the "Platform"). By creating an account or using Karigar, you agree to these Terms.

1. About Karigar
Karigar is a digital marketplace that connects customers with skilled service providers ("Workers") for home, commercial, maintenance, repair, installation, construction, cleaning, and other services. Karigar provides the technology platform; the actual services are performed by independent Workers, not by Karigar.

2. Eligibility
You must be at least 18 years old and legally capable of entering into a binding contract to use Karigar.

3. Accounts
You are responsible for the information you provide and for keeping your account secure. You must provide accurate, current, and complete details. You are responsible for all activity that occurs under your account.

4. Bookings
When you request a service, Karigar helps match you with a nearby Worker. Prices are shown before you confirm. A platform fee may apply and will be displayed before confirmation.

5. Payments
You agree to pay the amount shown for a booking, including any applicable platform fee. Payments may be collected in cash, UPI, or through third-party payment providers. You must not arrange off-platform payments to bypass platform fees where the Platform requires in-app payment.

6. Worker Verification
Workers may be asked to provide identity and verification documents. While Karigar takes reasonable steps to verify Workers, Karigar does not guarantee the quality, safety, or legality of any service.

7. Conduct
You agree to use the Platform lawfully and respectfully. Harassment, fraud, fake bookings, fake reviews, impersonation, and misuse of the Platform are prohibited and may result in suspension or termination.

8. Cancellations & Refunds
Cancellations and refunds are governed by our Refund & Cancellation Policy, which forms part of these Terms.

9. Ratings & Reviews
Customers may rate and review services. Reviews must be honest and may be publicly visible. Karigar may remove content that violates our policies.

10. Limitation of Liability
Karigar acts as an intermediary connecting customers and Workers. To the maximum extent permitted by law, Karigar is not liable for the acts, omissions, or conduct of Workers or customers, or for any indirect or consequential damages arising from use of the Platform.

11. Suspension & Termination
Karigar may suspend or terminate accounts that violate these Terms, our Community Guidelines, or applicable law.

12. Changes to These Terms
We may update these Terms from time to time. Material changes will be communicated through the Platform. Continued use after changes take effect constitutes acceptance.

13. Governing Law
These Terms are governed by the laws of India, and disputes are subject to the jurisdiction of the competent courts.

14. Contact
Karigar Support — Email: support@karigar.in — Website: https://karigar.in

By using Karigar, you acknowledge that you have read and understood these Terms and agree to be bound by them."""
    )

    val PRIVACY = LegalDoc(
        "privacy",
        "Privacy Policy",
        """Effective Date: July 6, 2026

Your privacy is important to us. This Privacy Policy explains how we collect, use, disclose, and protect your information when you use Karigar. By using our services, you agree to the practices described here.

1. About Karigar
Karigar is a digital marketplace that connects customers with verified skilled professionals for home, commercial, maintenance, repair, installation, construction, cleaning, and other services.

2. Information We Collect
Personal Information: full name, mobile number, email, date of birth (if required), profile photograph.
Location Information (with permission): live GPS location, approximate location, saved addresses, service location — used for finding nearby professionals, booking, navigation, fraud prevention, and safety.
Identity Verification (Workers): Aadhaar or other government ID, PAN (where applicable), selfie verification, address proof, skill certificates, police verification (where applicable), bank/UPI details — used only for verification and legal compliance.
Booking Information: service requests, dates, addresses, prices, status, cancellations, photos, ratings and reviews.
Payment Information: transaction ID, payment status, refund details, invoices. Karigar does NOT store your card details.
Communications: support conversations, in-app messages, complaint information.
Device Information: device model, OS, app version, IP address, device identifiers, crash reports, network information.

3. How We Use Your Information
To create and manage your account, match customers with professionals, process bookings and payments, verify identities, prevent fraud, improve services, provide support, send notifications and OTPs, resolve disputes, maintain security, and comply with legal obligations.

4. Information Sharing
We do not sell your personal information. We may share the minimum necessary information with the customers/workers in a booking, payment providers, cloud hosting providers, verification agencies, support vendors, analytics providers, and government or law enforcement where legally required.

5. Data Security
We use encryption of sensitive data in transit, secure cloud infrastructure, access controls, authentication measures, and regular monitoring. No method of electronic storage or transmission is completely secure.

6. Data Retention
We retain information only as long as necessary to provide services, maintain records, resolve disputes, prevent fraud, and comply with law. When no longer required, information is securely deleted or anonymized.

7. Your Rights
Subject to applicable law, you may access, update, correct, or delete your information, withdraw consent, and request a copy of your data. Some information may be retained where required by law.

8. Children's Privacy
Karigar is not intended for individuals under 18. We do not knowingly collect information from children.

9. Notifications
We may send OTP verification, booking updates, payment confirmations, service reminders, security alerts, and important announcements. Marketing messages are sent only where permitted, and you may opt out.

10. Account Deletion
You may request deletion of your account through the app or by contacting us. Some information may be retained for legal, tax, fraud-prevention, or regulatory purposes.

11. Contact
Karigar Support — Email: support@karigar.in — Website: https://karigar.in"""
    )

    val REFUND = LegalDoc(
        "refund",
        "Refund & Cancellation Policy",
        """Effective Date: July 6, 2026

This Refund & Cancellation Policy governs bookings made through the Karigar platform.

1. Cancellation by Customer
Customers may cancel a booking before the Service Provider starts traveling or begins the service. A cancellation fee may apply if the provider has already started traveling, the cancellation occurs shortly before the appointment, or repeated cancellations indicate misuse. Any fee is shown before confirmation.

2. Cancellation by Service Provider
If a provider cancels a confirmed booking, the customer is not charged, any payment already collected is refunded per this policy, and Karigar may help find another provider. Repeated cancellations by providers may lead to warnings, suspension, or removal.

3. Refund Eligibility
Refunds may be issued when payment was collected but the service was never provided, duplicate payments were made, a technical error caused an incorrect charge, a booking was cancelled per this policy, or Karigar determines a refund is appropriate. Refunds are generally not available simply because the customer changed their mind after satisfactory completion, was unavailable at the scheduled time, or provided incorrect booking details.

4. Service Quality Disputes
If a service was incomplete or materially different from what was agreed, report it to Karigar Support within 48 hours of completion. We may request photos, videos, invoices, or other evidence before deciding on any goodwill or partial refund.

5. Refund Processing
Approved refunds are generally processed to the original payment method. Processing times depend on the payment provider and banking system.

6. Platform Fees
Platform or convenience fees may be non-refundable where permitted by law, unless the booking failed due to a platform or payment error.

7. Final Decision
Karigar reserves the right to investigate refund requests and act fairly, reasonably, and in accordance with applicable law."""
    )

    val COMMUNITY = LegalDoc(
        "community",
        "Community Guidelines & Safety",
        """Effective Date: July 6, 2026

Karigar is committed to a safe and respectful platform for customers and Service Providers.

Respect
All users must treat one another with dignity and respect. Harassment, discrimination, abuse, threats, and hate speech are strictly prohibited.

Safety
Users must not engage in violence or intimidation, carry illegal weapons while using the platform, request or perform illegal services, or use alcohol or drugs in a way that creates safety risks while providing services. Customers should ensure a reasonably safe work environment; providers should use appropriate tools and safety equipment.

Fraud
Fake identities, false bookings, fake reviews, payment fraud, misrepresentation of skills, and impersonation are prohibited.

Off-Platform Payments
Where Karigar requires in-app payment, users must not bypass platform fees through unauthorized off-platform transactions.

Account Actions
Karigar may warn, suspend, or permanently terminate accounts that violate these guidelines.

Emergency Situations
If you believe there is an immediate risk to life, health, or property, contact local emergency services first. Karigar is not an emergency response provider."""
    )

    val all = listOf(TERMS, PRIVACY, REFUND, COMMUNITY)

    fun byKey(key: String?): LegalDoc? = all.firstOrNull { it.key == key }
}
