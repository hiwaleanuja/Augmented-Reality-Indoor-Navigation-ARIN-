package com.example.tejas.measureit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class NewProjectDialog extends AppCompatDialogFragment {

    private EditText projectTitle;
    private EditText projectDescription;

    private NewProjectDialogListener newProjectDialogListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view)
               .setTitle("New Project")
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {

                   }
               })
               .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       String title = projectTitle.getText().toString();
                       String description = projectDescription.getText().toString();
                       newProjectDialogListener.getInfo(title, description);
                   }
               });

        projectTitle = view.findViewById(R.id.projectTitle);
        projectDescription = view.findViewById(R.id.projectDescription);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            newProjectDialogListener = (NewProjectDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() +
                    "Must Implement NewProjectDialogListener");
        }
    }

    public interface NewProjectDialogListener{
        void getInfo(String title, String description);
    }
}
