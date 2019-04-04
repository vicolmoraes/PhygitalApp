package com.example.appphygital.view;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.appphygital.helper.ConfiguracaoFirebase;
import com.example.appphygital.R;
import com.example.appphygital.model.Visitante;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class TelaInicialActivity extends AppCompatActivity {

    private Button btnNovoVisitante;
    private Button btnAlanPedro, btnBenMeyer;

    private FirebaseAuth autenticacao;
    private FirebaseAuth mAuth;
    private Visitante visitante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicial);

        mAuth = FirebaseAuth.getInstance();

        //Verifica visitante logado
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        } else {
            verificarVisitanteLogado();
        }

        //Inicializar componentes
        inicializar();

        //Clique do botão
        botaoNovoVisitante();
        botaoAlanPedro();
        botaoBenMeyer();
    }

    private void botaoBenMeyer() {
        btnBenMeyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = "Ben Meyer";
                String phygits = "50";

                visitante = new Visitante();

                visitante.setNome(nome);
                visitante.setPhygits(phygits);

                cadastrarAnonimo(visitante);

            }
        });
    }

    private void botaoAlanPedro() {
        btnAlanPedro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nome = "Alan Pedro";
                String phygits = "50";

                visitante = new Visitante();

                visitante.setNome(nome);
                visitante.setPhygits(phygits);

                cadastrarAnonimo(visitante);


            }
        });
    }

    private void cadastrarAnonimo(final Visitante visitante) {

        mAuth.signInAnonymously().
                addOnCompleteListener(
                        this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    try {

                                        //Salvar dados no firebase
                                        String idVisitante = task.getResult().getUser().getUid();
                                        visitante.setId(idVisitante);
                                        visitante.salvar();

                                        startActivity(new Intent(getApplicationContext(), BoasVindasActivity.class));

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    String erroExecucao = "";
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthWeakPasswordException e) {
                                        erroExecucao = "Digite uma senha mais forte";
                                    } catch (FirebaseAuthInvalidCredentialsException e) {
                                        erroExecucao = "Por favor, digite um e-mail válido";
                                    } catch (FirebaseAuthUserCollisionException e) {
                                        erroExecucao = "Este conta já foi cadastra";
                                    } catch (Exception e) {
                                        erroExecucao = "Erro ao cadastrar usuário: " + e.getMessage();
                                        e.printStackTrace();
                                    }

                                    Toast.makeText(TelaInicialActivity.this, "Erro: " + erroExecucao, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
    }

    private void botaoNovoVisitante() {
        btnNovoVisitante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(TelaInicialActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scanner QRCode");
                integrator.setOrientationLocked(true);

                integrator.setBeepEnabled(true);
                integrator.setCameraId(0); // 0 = CAMERA TRASEIRA | FRONTAL = 1
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {

            if (result.getContents() != null){

                try{

                    String teste = result.getContents();

                    visitante = new Visitante();

                    String[] textoSepado = teste.split("\n");

                    visitante.setNome(textoSepado[0]);
                    visitante.setEmail(textoSepado[1]);
                    visitante.setEmpresa(textoSepado[2]);
                    visitante.setPhygits("0");

                    cadastrarAnonimo(visitante);

                } catch (Exception e){
                    Toast.makeText(this, "QRCode Inválido", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), CadastroActivity.class);
                    startActivity(intent);
                }

            } else {
                Intent intent = new Intent(getApplicationContext(), CadastroActivity.class);
                startActivity(intent);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void verificarVisitanteLogado() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if (autenticacao.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), BoasVindasActivity.class);
            startActivity(intent);
        }
    }

    private void inicializar() {
        btnNovoVisitante = findViewById(R.id.btnNovoVisitante);
        btnAlanPedro = findViewById(R.id.btnAlanPedro);
        btnBenMeyer = findViewById(R.id.btnBenMeyer);
    }

}