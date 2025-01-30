package com.christianserwedevs.doslocator.Activity.UserRegistration;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.christianserwedevs.doslocator.LoginActivity;
import com.christianserwedevs.doslocator.Prompts.ConfirmationDialog;
import com.christianserwedevs.doslocator.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserInformationRegistrationActivity extends AppCompatActivity {

    // First Information
    private EditText firstName, middleName, lastName, address;
    private Button buttonFirstInfoNext;

    // Parent Setup
    private EditText parentBirthdate, parentContact, parentEmail;
    private Button buttonFinishParent;

    // Child Setup
    private EditText childBirthdate, childContact, childEmail, childParentEmail;
    private Button buttonFinishChild;

    // Responder Setup
    private EditText responderContact, responderEmail, responderOrganizationName, responderOrganizationEmail;
    private Button buttonFinishResponder;


    private EditText parentPassword, childPassword, responderPassword;
    ScrollView parentOtherInformationSetup, childOtherInformationSetup, responderOtherInformationSetup, firstInformationGlobal;
    TextView textView_header;

    FirebaseFirestore firestoreDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_information_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            firestoreDatabase = FirebaseFirestore.getInstance();

            // Passwords
            parentPassword = findViewById(R.id.editText_parentUserPassword);
            childPassword = findViewById(R.id.editText_childUserPassword);
            responderPassword = findViewById(R.id.editText_responderUserPassword);

            // First Information
            firstName = findViewById(R.id.editText_firstName);
            middleName = findViewById(R.id.editText_middleName);
            lastName = findViewById(R.id.editText_lastName);
            address = findViewById(R.id.editText_address);
            buttonFirstInfoNext = findViewById(R.id.buttonFirstInfoNext);

            // Parent Information
            parentBirthdate = findViewById(R.id.editText_parentBirthdate);
            parentContact = findViewById(R.id.editText_parentContactNumber);
            parentEmail = findViewById(R.id.editText_parentEmailAddress);
            buttonFinishParent = findViewById(R.id.buttonFinish_ParentOtherInformationSetup);

            // Child Information
            childBirthdate = findViewById(R.id.editText_childBirthdate);
            childContact = findViewById(R.id.editText_childContactNumber);
            childEmail = findViewById(R.id.editText_childEmailAddress);
            childParentEmail = findViewById(R.id.editText_childParentEmailAddress);
            buttonFinishChild = findViewById(R.id.buttonFinish_ChildOtherInformationSetup);

            // Responder Information
            responderContact = findViewById(R.id.editText_responderContactNumber);
            responderEmail = findViewById(R.id.editText_responderEmailAddress);
            responderOrganizationName = findViewById(R.id.editText_responderOrganizationName);
            responderOrganizationEmail = findViewById(R.id.editText_responderOrganizationEmail);
            buttonFinishResponder = findViewById(R.id.buttonFinish_responderOtherInformationSetup);


            firstInformationGlobal = findViewById(R.id.firstInformationGlobal);
            parentOtherInformationSetup = findViewById(R.id.parentOtherInformationSetup);
            childOtherInformationSetup = findViewById(R.id.childOtherInformationSetup);
            responderOtherInformationSetup = findViewById(R.id.responderOtherInformationSetup);


            // Add DatePicker to Birthdate Fields
            setupDatePicker(parentBirthdate);
            setupDatePicker(childBirthdate);

            textView_header = findViewById(R.id.textView_TypeOfUserRegistration);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String data = bundle.getString("userType");
                if (data != null) {
                    switch (data) {
                        case "ParentType":
                            textView_header.setText("Parent");
                            break;

                        case "ChildType":
                            textView_header.setText("Child");
                            break;

                        case "ResponderType":
                            textView_header.setText("Responder");
                            break;

                        default:
                            textView_header.setText("User");
                            break;
                    }
                }
            }

            buttonFirstInfoNext = findViewById(R.id.buttonFirstInfoNext);

            buttonFirstInfoNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (validateFirstInfoFields()) {
                        ConfirmationDialog.show(
                                UserInformationRegistrationActivity.this,
                                "Confirmation",
                                "Please ensure all the information provided is correct before proceeding to the next part. Are you confirmed?",
                                new ConfirmationDialog.OnDialogClickListener() {
                                    @Override
                                    public void onConfirm() {
                                        nextRegistrationPart();
                                    }

                                    @Override
                                    public void onCancel() {
                                    }
                                }
                        );
                    }
                }
            });


            buttonFinishParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finishRegistration("Parent");
                }
            });
            buttonFinishChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finishRegistration("Child");
                }
            });
            buttonFinishResponder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finishRegistration("Responder");
                }
            });




            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    ConfirmationDialog.show(
                            UserInformationRegistrationActivity.this,
                            "Confirmation",
                            "If you go back, all input details will be cleared. Are you sure?",
                            new ConfirmationDialog.OnDialogClickListener() {
                                @Override
                                public void onConfirm() {
                                    finish();
                                }

                                @Override
                                public void onCancel() {

                                }
                            }
                    );
                }
            });

            return insets;
        });
    }


    private boolean validateFirstInfoFields() {
        String _firstName = firstName.getText().toString().trim();
        String _middleName = middleName.getText().toString().trim();
        String _lastName = lastName.getText().toString().trim();
        String _address = address.getText().toString().trim();

        if (_firstName.isEmpty()) {
            Toast.makeText(this, "First Name is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (_middleName.isEmpty()) {
            Toast.makeText(this, "Middle Name is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (_lastName.isEmpty()) {
            Toast.makeText(this, "Last Name is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (_address.isEmpty()) {
            Toast.makeText(this, "Address is required.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void finishRegistration(String userType) {
        String _firstName = firstName.getText().toString().trim();
        String _middleName = middleName.getText().toString().trim();
        String _lastName = lastName.getText().toString().trim();
        String _address = address.getText().toString().trim();

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", _firstName);
        userData.put("middleName", _middleName);
        userData.put("lastName", _lastName);
        userData.put("address", _address);

        switch (userType) {
            case "Parent":
                String _parentBirthdate = parentBirthdate.getText().toString().trim();
                String _parentContact = parentContact.getText().toString().trim();
                String _parentEmail = parentEmail.getText().toString().trim();
                String _parentPassword = parentPassword.getText().toString().trim();

                if (!validateFields(_parentBirthdate, _parentContact, _parentEmail, _parentPassword)) return;

                firestoreDatabase.collection("parents")
                        .whereEqualTo("email", _parentEmail)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                Toast.makeText(UserInformationRegistrationActivity.this, "Error: This email is already used.", Toast.LENGTH_LONG).show();
                            } else {
                                userData.put("birthdate", _parentBirthdate);
                                userData.put("contact", _parentContact);
                                userData.put("email", _parentEmail);
                                userData.put("password", _parentPassword);

                                addDataToFirestore("parents", userData);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(UserInformationRegistrationActivity.this, "Error checking email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            System.err.println("Error checking email: " + e.getMessage());
                        });
                break;


            case "Child":
                String _childBirthDate = childBirthdate.getText().toString().trim();
                String _childContact = childContact.getText().toString().trim();
                String _childEmail = childEmail.getText().toString().trim();
                String _childParentEmail = childParentEmail.getText().toString().trim();
                String _childPassword = childPassword.getText().toString().trim();

                if (!validateFields(_childBirthDate, _childContact, _childEmail, _childParentEmail, _childPassword)) return;

                userData.put("birthdate", _childBirthDate);
                userData.put("contact", _childContact);
                userData.put("email", _childEmail);
                userData.put("parentEmail", _childParentEmail);
                userData.put("password", _childPassword);

                firestoreDatabase.collection("parents")
                        .whereEqualTo("email", _childParentEmail)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                addDataToFirestore("children", userData);
                            } else {
                                Toast.makeText(UserInformationRegistrationActivity.this, "Error: Parent email does not exist.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(UserInformationRegistrationActivity.this, "Error checking parent email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            System.err.println("Error checking parent email: " + e.getMessage());
                        });
                break;

            case "Responder":
                String _responderContact = responderContact.getText().toString().trim();
                String _responderEmail = responderEmail.getText().toString().trim();
                String _responderOrganizationName = responderOrganizationName.getText().toString().trim();
                String _responderOrganizationEmail = responderOrganizationEmail.getText().toString().trim();
                String _responderPassword = responderPassword.getText().toString().trim();

                if (!validateFields(_responderContact, _responderEmail, _responderOrganizationName, _responderOrganizationEmail, _responderPassword)) return;

                userData.put("contact", _responderContact);
                userData.put("email", _responderEmail);
                userData.put("organizationName", _responderOrganizationName);
                userData.put("organizationEmail", _responderOrganizationEmail);
                userData.put("password", _responderPassword);

                addDataToFirestore("responders", userData);
                break;
        }
    }

    private boolean validateFields(String... fields) {
        for (String field : fields) {
            if (field.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void addDataToFirestore(String collectionName, Map<String, Object> data) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        data.put("createdAt", currentTime);

        firestoreDatabase.collection(collectionName)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();

                    documentReference.update("userId", documentId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UserInformationRegistrationActivity.this, "Registration Completed. You will now redirected to login", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(UserInformationRegistrationActivity.this, LoginActivity.class));
                                finish();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(UserInformationRegistrationActivity.this, "Error adding document: " + e.getMessage(), Toast.LENGTH_LONG).show();

                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserInformationRegistrationActivity.this, "Error adding document: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    System.err.println("Error adding document: " + e.getMessage());
                });
    }



    private void nextRegistrationPart(){
        firstInformationGlobal.setVisibility(View.INVISIBLE);

        switch (textView_header.getText().toString()) {
            case "Parent":
                parentOtherInformationSetup.setVisibility(View.VISIBLE);
                break;
            case "Child":
                childOtherInformationSetup.setVisibility(View.VISIBLE);

                break;
            case "Responder":
                responderOtherInformationSetup.setVisibility(View.VISIBLE);

                break;
            default:
                break;
        }
    }
    private void setupDatePicker(EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    UserInformationRegistrationActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear);
                        editText.setText(formattedDate);
                    },
                    year, month, day
            );

            // Optional: Set maximum date to current date
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }


}