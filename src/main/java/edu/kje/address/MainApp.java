package edu.kje.address;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import edu.kje.address.model.Person;
import edu.kje.address.model.PersonListWrapper;
import edu.kje.address.view.BirthdayStatisticsController;
import edu.kje.address.view.PersonEditDialogController;
import edu.kje.address.view.PersonOverviewController;
import edu.kje.address.view.RootLayoutController;

/**
 * JavaFX App made following a tutorial by Marco Jakob. 
 * Website: https://code.makery.ch
 * @author Kevin El-Saikali
 * @version 2022-06-16
 * 
 */
public class MainApp extends Application {

    /**
     * The data is an observable list of Persons
     */

	private ObservableList<Person> personData = FXCollections.observableArrayList(); //Permet de savoir quand un changement arrive aux informations du document

	/**
	 * Constructor
	 */
	public MainApp() {
		// Add some sample data
		personData.add(new Person("Hans", "Muster"));
		personData.add(new Person("Ruth", "Mueller"));
		personData.add(new Person("Heinz", "Kurz"));
		personData.add(new Person("Cornelia", "Meier"));
		personData.add(new Person("Werner", "Meyer"));
		personData.add(new Person("Lydia", "Kunz"));
		personData.add(new Person("Anna", "Best"));
		personData.add(new Person("Stefan", "Meier"));
		personData.add(new Person("Martin", "Mueller"));
	}
  
	/**
	 * Returns the data as an observable list of Persons. 
	 * @return
	 */
	public ObservableList<Person> getPersonData() {
		return personData;
	}

    private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage; //MainApp est maintenant le centrale de l'application
        this.primaryStage.setTitle("Contacts App"); //Nommer notre application Address App

        //set the app icon
        this.primaryStage.getIcons().add(new Image("file:src/main/resources/images/address_icon_32.png")); //Ajoute le icone de l'application

        initRootLayout();
        showPersonOverview();

    }


    public void initRootLayout(){  //La forme de base de notre programme
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml")); 
            rootLayout = (BorderPane) loader.load(); //Les informations dans rootlayout sont téléchargés
    
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout); 
            primaryStage.setScene(scene); //Cree par javafx et non par l'application 
    
            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this); 
    
            primaryStage.show(); //Montrer le layout cree par javafx a l'ecran
        } catch (IOException e) { //Prendre soin des exceptions comme si le fxml n'existe pas
            e.printStackTrace();
        }
    
        // Try to load last opened person file.
        File file = getPersonFilePath();
        if (file != null) { //Autant qu'un fichier a déjà été créé, on va afficher celui le plus récemment fermé
            loadPersonDataFromFile(file);
        }
    }

    /**
     * Shows the person overview inside the root layout 
     * */

    public void showPersonOverview() {
        
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PersonOverview.fxml")); //Cherche les infos dans le fichier .fxml
            AnchorPane personOverview = (AnchorPane) loader.load(); //Insérer les informations trouvés dans le .fxml dans l'application

            // Set person overview into the center of the root layout
            rootLayout.setCenter(personOverview);

            //Give the controller access to the main app
            PersonOverviewController controller = loader.getController(); //Permet au controlleur d'utiliser les données retrouvés dans PersonOverview
            controller.setMainApp(this);

        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Permet à l'utilisateur d'étider les informations existantes.  
     * On recherche les informations trouvés dans le fichier .fxml pour le fenêtre d'édition
     * et on donne l'option de les changer
     * @param person
     * @return
     */
    public boolean showPersonEditingDialog(Person person){
        try{
            //Load the fxml file and create a new stage and popup dialog
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PersonEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            //Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Person Details");
            dialogStage.getIcons().add(new Image("file:src/main/resources/images/edit_info_33.png"));
            dialogStage.initModality(Modality.WINDOW_MODAL); //Prévient que des évènements se font délivrés à d'autres fenêtres de l'application (un input... etc.)
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene); //Afficher la page d'édition

            //Set the person into the controller
            PersonEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person);

            //Show the dialog window until the user closes it
            dialogStage.showAndWait();

            return controller.isOKClicked();

        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the person file preference, i.e. the file that was last opened.
     * The preference is read from the OS specific registry. If no such
     * preference can be found, null is returned.
     * 
     * @return
     */
    public File getPersonFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) { //Si un chemin au fichier existe déjà, retourne cette chemin pour l'afficher quand on ouvre l'application de nouveau
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     * 
     * @param file the file or null to remove the path
     */
    public void setPersonFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Update the stage title.
            primaryStage.setTitle("AddressApp - " + file.getName()); //Le titre de l'application va être le nom avec lequel vous avez sauvegardé votre fichier
        } else {
            prefs.remove("filePath");

            // Update the stage title.
            primaryStage.setTitle("AddressApp");
        }
    }

    public void loadPersonDataFromFile(File file){
        try{
            JAXBContext context = JAXBContext.newInstance(PersonListWrapper.class);
            Unmarshaller um = context.createUnmarshaller(); //Permet de lire le XML et le convertir en Java

            //Reading XML from the file and unmarshalling
            PersonListWrapper wrapper = (PersonListWrapper) um.unmarshal(file); //Lire le xml qui seretrouve dans le fichier

            personData.clear();
            personData.addAll(wrapper.getPersons());

            //Save the file path to the registry
            setPersonFilePath(file);
        } catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR); //Si les données ne sont pas là afficher une alerte
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    /**
     * Saves the current person data to the specified file
     * 
     * @param file
     */

    public void savePersonDataToFile(File file){
        try {
            JAXBContext context = JAXBContext.newInstance(PersonListWrapper.class);
            Marshaller m = context.createMarshaller(); //Fais l'opposé du unmarshaller. Celui ci prend le java et le converti en xml
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); //Utilisé pour formatter le xml qui est traduit
    
            // Wrapping our person data.
            PersonListWrapper wrapper = new PersonListWrapper();
            wrapper.setPersons(personData);
    
            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file); 
    
            // Save the file path to the registry.
            setPersonFilePath(file);
        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());
    
            alert.showAndWait();
        }

        
    }

    /**
     * Opens a dialog to show birthday statistics.
     */
    public void showBirthdayStatistics() {
        try {
            // Load the fxml file and create a new stage for the popup.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/BirthdayStatistics.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Birthday Statistics");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the persons into the controller.
            BirthdayStatisticsController controller = loader.getController();
            controller.setPersonData(personData);

            dialogStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the main stage
     * @return
     */
    public Stage getPrimaryStage(){
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

}