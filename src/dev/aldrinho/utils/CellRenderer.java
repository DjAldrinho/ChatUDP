package dev.aldrinho.utils;


import dev.aldrinho.models.Usuario;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;


public class CellRenderer implements Callback<ListView<Usuario>,ListCell<Usuario>>{
        @Override
    public ListCell<Usuario> call(ListView<Usuario> p) {

            return new ListCell<Usuario>(){

                @Override
                protected void updateItem(Usuario user, boolean bln) {
                    super.updateItem(user, bln);
                    setGraphic(null);
                    setText(null);
                    if (user != null) {
                        HBox hBox = new HBox();

                        Text name = new Text(user.getUsuario());

                        ImageView statusImageView = new ImageView();
                        Image statusImage = new Image(getClass().getClassLoader().getResource("images/" + user.getEstado() + ".png").toString(), 16, 16,true,true);
                        statusImageView.setImage(statusImage);

                        ImageView pictureImageView = new ImageView();
                        Image image = new Image(getClass().getClassLoader().getResource("images/" + user.getLogo() + ".png").toString(),50,50,true,true);
                        pictureImageView.setImage(image);

                        hBox.getChildren().addAll(statusImageView, pictureImageView, name);
                        hBox.setAlignment(Pos.CENTER_LEFT);

                        setGraphic(hBox);
                    }
                }
            };
    }
}