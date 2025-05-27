package de.plaxxo.dzm.views.userManagement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.plaxxo.dzm.data.Role;
import de.plaxxo.dzm.data.User;
import de.plaxxo.dzm.services.UserService;
import de.plaxxo.dzm.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.Arrays;

@PageTitle("Benutzerverwaltung")
@Route(value = "admin/users", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ADMIN"})
@Menu(order = 1, icon = LineAwesomeIconUrl.USERS_SOLID)
public class UserManagementView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class);

    public UserManagementView(UserService userService) {
        this.userService = userService;

        add(new H2("Benutzerverwaltung"));

        Button addButton = new Button("Neuen Benutzer anlegen", e -> openUserDialog(null));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        configureGrid();

        add(addButton, grid);
        refreshGrid();

        setSizeFull();
    }

    private void configureGrid() {
        grid.setColumns("username", "name");

        grid.addColumn(user -> user.getRoles().toString())
                .setHeader("Rollen");

        grid.addComponentColumn(user -> {
            Button editButton = new Button("Bearbeiten", e -> openUserDialog(user));
            Button deleteButton = new Button("Löschen", e -> deleteUser(user));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Aktionen");


        grid.setSizeFull();
    }

    private void refreshGrid() {
        grid.setItems(userService.findAllUsers());
    }

    private void openUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        boolean isNewUser = (user == null);
        user = isNewUser ? new User() : user;
        User currentUser = user;

        H2 title = new H2(isNewUser ? "Neuen Benutzer anlegen" : "Benutzer bearbeiten");

        TextField username = new TextField("Benutzername");
        username.setRequired(true);
        if (!isNewUser) {
            username.setValue(currentUser.getUsername());
            username.setReadOnly(true);
        }

        TextField name = new TextField("Name");
        name.setRequired(true);
        if (!isNewUser) {
            name.setValue(currentUser.getName());
        }

        PasswordField password = new PasswordField("Passwort");
        password.setRequired(isNewUser);

        CheckboxGroup<Role> roles = new CheckboxGroup<>();
        roles.setLabel("Rollen");
        roles.setItems(Arrays.asList(Role.values()));
        if (!isNewUser && currentUser.getRoles() != null) {
            roles.setValue(currentUser.getRoles());
        } else {
            roles.select(Role.USER);
        }

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());

        Button saveButton = new Button("Speichern", e -> {
            try {
                if (isNewUser) {
                    if (username.isEmpty() || password.isEmpty()) {
                        Notification.show("Benutzername und Passwort sind erforderlich")
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }

                    userService.createUser(
                            username.getValue(),
                            name.getValue(),
                            password.getValue(),
                            roles.getValue().toArray(new Role[0]));
                } else {
                    currentUser.setName(name.getValue());
                    currentUser.setRoles(roles.getValue());

                    if (!password.isEmpty()) {
                        currentUser.setHashedPassword(userService.getPasswordEncoder().encode(password.getValue()));
                    }

                    userService.updateUser(currentUser);
                }

                refreshGrid();
                dialog.close();

                Notification.show(isNewUser ? "Benutzer erstellt" : "Benutzer aktualisiert")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(title, username, name, password, roles, buttons);
        dialog.open();
    }

    private void deleteUser(User user) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.add(
                new H2("Benutzer löschen"),
                new Paragraph("Sind Sie sicher, dass Sie den Benutzer '" + user.getUsername() + "' löschen möchten?"));

        Button cancelButton = new Button("Abbrechen", e -> confirmDialog.close());
        Button confirmButton = new Button("Löschen", e -> {
            userService.deleteUser(user.getId());
            refreshGrid();
            confirmDialog.close();
            Notification.show("Benutzer wurde gelöscht")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        confirmDialog.add(new HorizontalLayout(cancelButton, confirmButton));
        confirmDialog.open();
    }
}