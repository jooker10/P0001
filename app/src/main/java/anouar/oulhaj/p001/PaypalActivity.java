package anouar.oulhaj.p001;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class PaypalActivity extends AppCompatActivity {
    public static final String clientKey = "AUvSv5PZOScx3mJPksdgnFdxaw-qj6XXNzNha_DvF0zpF4H0g9mfANN4M_eMgG2in8pdzowQOosTtuXw";
    public static final int PAYPAL_REQUEST_CODE = 123;
    public static final String ARG_PAYMENT_AMOUNT = "amount";
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready,
            // switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            // on below line we are passing a client id.
            .clientId(clientKey);
    private TextView tvAmount;
    private TextView paymentTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal);



        double amount_paypal =getIntent().getDoubleExtra(ARG_PAYMENT_AMOUNT,10);


            // on below line we are initializing our variables.
            tvAmount = findViewById(R.id.etPaypalAmount);

            // creating a variable for button, edit text and status tv.
            Button makePaymentBtn = findViewById(R.id.BtnPay);
            paymentTV = findViewById(R.id.txtStatus);

        tvAmount.setText(String.valueOf(amount_paypal));

            // on below line adding click listener to our make payment button.
            makePaymentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // calling a method to get payment.
                    getPayment();
                }
            });
        }

        private void getPayment() {

            // Getting the amount from editText
            String amount = tvAmount.getText().toString();

            // Creating a paypal payment on below line.
            PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(amount)), "USD", "ANOUAR OULHAJ",
                    PayPalPayment.PAYMENT_INTENT_SALE);

            // Creating Paypal Payment activity intent
            Intent intent = new Intent(this, PaymentActivity.class);

            //putting the paypal configuration to the intent
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

            // Putting paypal payment to the intent
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

            // Starting the intent activity for result
            // the request code will be used on the method onActivityResult
            startActivityForResult(intent, PAYPAL_REQUEST_CODE);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // If the result is from paypal
            if (requestCode == PAYPAL_REQUEST_CODE) {

                // If the result is OK i.e. user has not canceled the payment
                if (resultCode == Activity.RESULT_OK) {

                    // Getting the payment confirmation
                    PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                    // if confirmation is not null
                    if (confirm != null) {
                        try {
                            // Getting the payment details
                            String paymentDetails = confirm.toJSONObject().toString(4);
                            // on below line we are extracting json response and displaying it in a text view.
                            JSONObject payObj = new JSONObject(paymentDetails);
                            String payID = payObj.getJSONObject("response").getString("id");
                            String state = payObj.getJSONObject("response").getString("state");
                            paymentTV.setText("Payment " + state + "\n with payment id is " + payID);
                        } catch (JSONException e) {
                            // handling json exception on below line
                            Log.e("Error", "an extremely unlikely failure occurred: ", e);
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // on below line we are checking the payment status.
                    Log.i("paymentExample", "The user canceled.");
                } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                    // on below line when the invalid paypal config is submitted.
                    Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
                }
            }
        }
    }
