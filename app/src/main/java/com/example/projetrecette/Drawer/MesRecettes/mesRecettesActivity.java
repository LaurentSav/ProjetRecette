package com.example.projetrecette.Drawer.MesRecettes;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetrecette.GlideApp;
import com.example.projetrecette.R;
import com.example.projetrecette.Recette.RecipeModel;
import com.example.projetrecette.Recette.newRecetteActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class mesRecettesActivity extends AppCompatActivity {

    ImageView btnAddRecette;
    StorageReference mStorageRef;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    RecyclerView mResultList;
    FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mes_recettes);
        btnAddRecette = findViewById(R.id.add_recette);
        addToolbar();
        setAttribut();
        onClickAddRecette();
        getQuery();

    }

    public void getQuery(){
        Query query = fStore.collection("recipes").whereEqualTo("Auteur", userId);
        FirestoreRecyclerOptions<RecipeModel> options = new FirestoreRecyclerOptions.Builder<RecipeModel>().setQuery(query, RecipeModel.class).build();
        adapter = new FirestoreRecyclerAdapter<RecipeModel, RecipeModelViewHolder>(options) {

            @NonNull
            @Override
            public RecipeModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_recipe, parent, false);
                return new RecipeModelViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RecipeModelViewHolder holder, int position, @NonNull RecipeModel model) {
                holder.setRecipe(model);
            }
        };

        mResultList.setLayoutManager(new LinearLayoutManager(this));
        mResultList.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    private class RecipeModelViewHolder extends RecyclerView.ViewHolder{

        TextView name, author, cookingtime;
        RatingBar rating;
        ImageView image;



        public RecipeModelViewHolder(@NonNull View itemView){
            super(itemView);
            this.name = itemView.findViewById(R.id.recipe_name);
            this.author = itemView.findViewById(R.id.recipe_author);
            this.cookingtime = itemView.findViewById(R.id.recipe_time);
            this.rating = itemView.findViewById(R.id.recipe_rating);
            this.image = itemView.findViewById(R.id.recipe_image);
        }

        public void setRecipe(RecipeModel recipe){
            String temps = "Temps : " + recipe.getTemps_Cuisson() + " minutes";
            this.name.setText(recipe.getNom_Recette());
            this.cookingtime.setText(temps);
            this.rating.setRating(Float.parseFloat(recipe.getRating()));

            final TextView aut = this.author;
            final ImageView img = this.image;
            fStore = FirebaseFirestore.getInstance();
            fStore.collection("users").document(recipe.getAuteur()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String auteur = "Par " + documentSnapshot.getString("Fullname");
                    aut.setText(auteur);
                }
            });
            fStore.collection("recipes").document(recipe.getRecipe_id()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    final StorageReference pathphoto1 = mStorageRef.child("Recipes_pics").child(documentSnapshot.getString("Recipe_Pic"));
                    /* Si problème rebuild le projet */
                    GlideApp.with(getApplicationContext()).load(pathphoto1).into(img);
                }
            });

        }

    }


    public void onClickAddRecette(){
        btnAddRecette.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), newRecetteActivity.class);
                startActivity(intent);
            }
        }));
    }

    public void setAttribut(){
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        mResultList = findViewById(R.id.recycler_view);
        mStorageRef = FirebaseStorage.getInstance().getReference();

    }

    public void addToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}