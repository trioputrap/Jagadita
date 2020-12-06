package com.example.uts.jagadita;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uts.jagadita.api.ApiClient;
import com.example.uts.jagadita.api.ApiService;
import com.example.uts.jagadita.models.ApiResponse;
import com.example.uts.jagadita.models.Perusahaan;
import com.example.uts.jagadita.models.Transaksi;
import com.example.uts.jagadita.utils.PreferencesHelper;
import com.google.gson.Gson;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.core.UIKitCustomSetting;
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.snap.Authentication;
import com.midtrans.sdk.corekit.models.snap.CreditCard;
import com.midtrans.sdk.corekit.models.snap.CustomerDetails;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;


public class DetailUsahaActivity extends AppCompatActivity implements TransactionFinishedCallback {
    public boolean statusPembeli = true;
    public static final String EXTRA_SESSION_ID = "DATA_PERUSAHAAN";

    TextView nama_perusahaan;
    TextView jenis_usaha;
    TextView total_saham;
    TextView nama_pemilik;
    TextView telp;
    TextView alamat;
    TextView harga;
    TextView deskripsi;

    Button btnBeli;
    Button btnDonatur;

    Perusahaan perusahaan;

    ApiService apiService;
    PreferencesHelper preferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_usaha);

        apiService = ApiClient.getService(this);
        preferencesHelper = new PreferencesHelper(this);

        getSupportActionBar().setTitle("Informasi Perusahaan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //get perusahaan data
        String perusahaanJson = getIntent().getStringExtra(EXTRA_SESSION_ID);
        perusahaan = new Gson().fromJson(perusahaanJson, Perusahaan.class);

        statusPembeli = !(preferencesHelper.getUserId() == perusahaan.getId_pemilik());

        nama_perusahaan = findViewById(R.id.nama_perusahaan);
        jenis_usaha = findViewById(R.id.jenis_usaha);
        total_saham = findViewById(R.id.total_saham);
        nama_pemilik = findViewById(R.id.nama_pemilik);
        telp = findViewById(R.id.telp);
        alamat = findViewById(R.id.alamat);
        harga = findViewById(R.id.harga);
        deskripsi = findViewById(R.id.deskripsi);
        btnBeli = findViewById(R.id.btnBeli);
        btnDonatur = findViewById(R.id.btnDonatur);

        if(!statusPembeli){
            btnBeli.setText("Lihat Pembeli");
            btnDonatur.setText("Lihat Donatur");
        }

        nama_perusahaan.setText(perusahaan.getNama_perusahaan());
        jenis_usaha.setText(perusahaan.getJenis_usaha());
        total_saham.setText("Rp"+String.valueOf(perusahaan.getTotal_saham()));
        nama_pemilik.setText(perusahaan.getNama_pemilik());
        telp.setText(perusahaan.getTelp());
        alamat.setText(perusahaan.getAlamat()+"\n"+perusahaan.getKota());
        harga.setText("Rp"+String.valueOf(perusahaan.getHarga()));
        deskripsi.setText(perusahaan.getDeskripsi());


        btnBeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(statusPembeli){
                    clickPay(perusahaan);
                } else {
                    Intent transaksi_list = new Intent(view.getContext(), ListTransaksiActivity.class);
                    transaksi_list.putExtra(ListTransaksiActivity.EXTRA_SESSION_ID, new Gson().toJson(perusahaan));
                    view.getContext().startActivity(transaksi_list);
                }
            }
        });
        makePayment();
    }

    private void clickPay(Perusahaan perusahaan){
        MidtransSDK.getInstance().setTransactionRequest(transactionRequest(String.valueOf(perusahaan.getId()),perusahaan.getHarga(), 1, "Saham" + perusahaan.getNama_perusahaan()));
        UIKitCustomSetting setting = MidtransSDK.getInstance().getUIKitCustomSetting();
        setting.setSkipCustomerDetailsPages(true);
        MidtransSDK.getInstance().setUIKitCustomSetting(setting);

        MidtransSDK.getInstance().startPaymentUiFlow(this );
    }

    private void makePayment(){
        SdkUIFlowBuilder.init()
                .setContext(this)
                .setMerchantBaseUrl(BuildConfig.MERCHANT_BASE_URL)
                .setClientKey(BuildConfig.MERCHANT_CLIENT_KEY)
                .setTransactionFinishedCallback(this)
                .enableLog(true)
                .setColorTheme(new CustomColorTheme("#777777","#f77474" , "#3f0d0d"))
                .buildSDK();
    }

    public static TransactionRequest transactionRequest(String id, int price, int qty, String name){
        TransactionRequest request =  new TransactionRequest(System.currentTimeMillis() + " " , price );
        ItemDetails details = new ItemDetails(id, price, qty, name);

        ArrayList<ItemDetails> itemDetails = new ArrayList<>();
        itemDetails.add(details);
        request.setItemDetails(itemDetails);
        CreditCard creditCard = new CreditCard();
        creditCard.setSaveCard(false);
        creditCard.setAuthentication(Authentication.AUTH_RBA);

        request.setCreditCard(creditCard);
        return request;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTransactionFinished(TransactionResult result) {
        String status = "";
        if(result.getResponse() != null){
            switch (result.getStatus()){
                case TransactionResult.STATUS_SUCCESS:
                    Toast.makeText(this, "Transaction Sukses " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    status = "berhasil";
                    break;
                case TransactionResult.STATUS_PENDING:
                    Toast.makeText(this, "Transaction Pending " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    status = "pending";
                    break;
                case TransactionResult.STATUS_FAILED:
                    Toast.makeText(this, "Transaction Failed" + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    status = "gagal";
                    break;
            }
            result.getResponse().getValidationMessages();
        }else if(result.isTransactionCanceled()){
            Toast.makeText(this, "Transaction Cancelled", Toast.LENGTH_LONG).show();
            status = "dibatalkan";
        }else{
            if(result.getStatus().equalsIgnoreCase((TransactionResult.STATUS_INVALID))){
                Toast.makeText(this, "Transaction Invalid" + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "Something Wrong", Toast.LENGTH_LONG).show();
            }
            status = "gagal";
        }

        String gross_amount = result.getResponse().getGrossAmount().replace(".00", "");
        Transaksi transaksi = new Transaksi(
                preferencesHelper.getUserId(),
                perusahaan.getId(),
                Integer.parseInt(gross_amount),
                status,
                result.getResponse().getTransactionId()
        );
        apiService.create_transaksi(transaksi)
                .enqueue(new DetailUsahaActivity.CreateCallback());
    }

    public class CreateCallback implements retrofit2.Callback<ApiResponse> {
        @Override
        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
            ApiResponse apiResponse = response.body();
            if(response.isSuccessful() && apiResponse.getStatus()){

                Toast.makeText(DetailUsahaActivity.this, "Berhasil menyimpan data transaksi", Toast.LENGTH_LONG).show();

                finish();
            } else {
                Toast.makeText(DetailUsahaActivity.this, "Gagal menyimpan data transaksi", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<ApiResponse> call, Throwable t) {
            Toast.makeText(DetailUsahaActivity.this, "Gagal menyimpan data transaksi", Toast.LENGTH_LONG).show();
        }
    }
}