package com.example.Lab7MonoApi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String apiUrl = "https://api.monobank.ua/bank/";
    private List<CurrencyPojo> postMonoApi;
    private RadioGroup radioGroup;
    private final Map<String, Integer> currencyCodes = new HashMap<String, Integer>() {{
        put("XPD", 964);
        put("PLN", 985);
        put("UAH", 980);
        put("USD", 840);
        put("EUR", 978);
    }};
//    FirebaseFirestore firestore;
    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        firestore = FirebaseFirestore.getInstance();
//        Map<String,Object> users = new HashMap<>();
//        users.put("firstName","EASY");
//        users.put("lasName","TUTO");
//        users.put("description","Subscribe");
//        firestore.collection("users").add(users).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//            @Override
//            public void onSuccess(DocumentReference documentReference) {
//                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(),"Failue",Toast.LENGTH_LONG).show();
//            }
//        });
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        }else{
            textView.setText(user.getEmail());
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            }
        });
        Button convertButton = findViewById(R.id.convertButton);
        Spinner forCurrencyA = findViewById(R.id.forCurrencyA);
        Spinner forCurrencyB = findViewById(R.id.forCurrencyB);
        EditText textExchangeRates = findViewById(R.id.textExchangeRates);
        EditText dataInput = findViewById(R.id.dataInput);
        EditText myresult = findViewById(R.id.myresult);
        radioGroup = findViewById(R.id.radioGroup);
        getDataFromApi();
        convertButton.setOnClickListener(e -> {
            if (postMonoApi != null) {
                double myData = Double.parseDouble(dataInput.getText().toString());
                int currencyA = currencyCodes.get(forCurrencyA.getSelectedItem().toString());
                int currencyB = currencyCodes.get(forCurrencyB.getSelectedItem().toString());
                if (currencyA == currencyB) {
                    textExchangeRates.setText("U cannot convert " + forCurrencyA.getSelectedItem().toString() + " in " + forCurrencyB.getSelectedItem().toString());
                } else {
                    try {
                        double res = (convert(postMonoApi, 1, String.valueOf(currencyA), String.valueOf(currencyB)));
                        textExchangeRates.setText(new DecimalFormat("#0.00").format(res));
                        myresult.setText(new DecimalFormat("#0.00").format(res * myData));

                    } catch (Exception ex) {
                        myresult.setText("༼ つ ◕_◕ ༽つError༼ つ ◕_◕ ༽つ");
                        textExchangeRates.setText(" ");
                        ex.printStackTrace();
                    }
                }
            } else {
                textExchangeRates.setText("Just wait");
            }
        });
    }

    public Double convert(@NonNull List<CurrencyPojo> currenciesList, double myData, String fromCurrency, String toCurrency) {
        int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton myRadioButton = findViewById(checkedRadioButtonId);
        int checkedIndex = radioGroup.indexOfChild(myRadioButton);
        for (CurrencyPojo currency : currenciesList) {
            if (currency.getCurrencyCodeA() == Integer.parseInt(fromCurrency) && currency.getCurrencyCodeB() == Integer.parseInt(toCurrency)) {
                switch (checkedIndex) {
                    case 0: {
                        return myData * currency.getRateSell();
                    }
                    case 1: {
                        return myData * currency.getRateBuy();
                    }
                    case 2: {
                        return myData * currency.getRateCross();
                    }
                }
            }
            if (currency.getCurrencyCodeA() == Integer.parseInt(toCurrency) && currency.getCurrencyCodeB() == Integer.parseInt(fromCurrency)) {
                switch (checkedIndex) {
                    case 0: {
                        return myData / currency.getRateSell();
                    }
                    case 1: {
                        return myData / currency.getRateBuy();
                    }
                    case 2: {
                        return myData / currency.getRateCross();
                    }
                }
            }
        }
        return null;
    }

    private void getDataFromApi() {
        NetworkService.getInstance(apiUrl)
                .getJSONApi()
                .getAllCurrencies()
                .enqueue(new Callback<List<CurrencyPojo>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CurrencyPojo>> call, @NonNull Response<List<CurrencyPojo>> response) {
                        Thread myThread = new Thread(() -> {
                            while (true) {
                                postMonoApi = response.body();
                                try {
                                    Thread.sleep(40000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        });
                        myThread.start();
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<CurrencyPojo>> call, @NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });
    }
}
