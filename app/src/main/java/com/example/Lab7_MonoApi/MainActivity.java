package com.example.Lab7_MonoApi;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                double amount = Double.parseDouble(dataInput.getText().toString());
                int currencyA = currencyCodes.get(forCurrencyA.getSelectedItem().toString());
                int currencyB = currencyCodes.get(forCurrencyB.getSelectedItem().toString());
                 if (currencyA == currencyB) {
                    textExchangeRates.setText("U cannot convert " + forCurrencyA.getSelectedItem().toString() + " in " + forCurrencyB.getSelectedItem().toString());
                } else {
                    try {
                        double res = (convert(postMonoApi, 1, String.valueOf(currencyA), String.valueOf(currencyB)));
                        textExchangeRates.setText(new DecimalFormat("#0.00").format(res));
                        myresult.setText(new DecimalFormat("#0.00").format(res * amount));

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

    public Double convert(@NonNull List<CurrencyPojo> currenciesList,
                          double amount, String fromCurrency, String toCurrency) {
        int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton myRadioButton = findViewById(checkedRadioButtonId);
        int checkedIndex = radioGroup.indexOfChild(myRadioButton);

        for (CurrencyPojo currency : currenciesList) {
            if (currency.getCurrencyCodeA() == Integer.parseInt(fromCurrency) && currency.getCurrencyCodeB() == Integer.parseInt(toCurrency)) {
                switch (checkedIndex) {
                    case 0: {
                        return amount * currency.getRateSell();
                    }
                    case 1: {
                        return amount * currency.getRateBuy();
                    }
                    case 2: {
                        return amount * currency.getRateCross();
                    }
                }
            }
            if (currency.getCurrencyCodeA() == Integer.parseInt(toCurrency) && currency.getCurrencyCodeB() == Integer.parseInt(fromCurrency)) {
                switch (checkedIndex) {
                    case 0: {
                        return amount / currency.getRateSell();
                    }
                    case 1: {
                        return amount / currency.getRateBuy();
                    }
                    case 2: {
                        return amount / currency.getRateCross();
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
