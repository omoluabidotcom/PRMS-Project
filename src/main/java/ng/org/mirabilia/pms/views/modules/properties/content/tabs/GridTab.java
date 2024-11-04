package ng.org.mirabilia.pms.views.modules.properties.content.tabs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.server.StreamResource;
import ng.org.mirabilia.pms.domain.entities.City;
import ng.org.mirabilia.pms.domain.entities.Phase;
import ng.org.mirabilia.pms.domain.entities.Property;
import ng.org.mirabilia.pms.domain.entities.State;
import ng.org.mirabilia.pms.domain.enums.PropertyStatus;
import ng.org.mirabilia.pms.domain.enums.PropertyType;
import ng.org.mirabilia.pms.services.*;
import ng.org.mirabilia.pms.views.forms.properties.AddPropertyForm;
import ng.org.mirabilia.pms.views.forms.properties.EditPropertyForm;

import java.io.ByteArrayInputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GridTab extends VerticalLayout {
    private final PropertyService propertyService;
    private final PhaseService phaseService;
    private final CityService cityService;
    private final StateService stateService;
    private final UserService userService;

    private final Grid<Property> propertyGrid;
    private final TextField searchField;
    private final ComboBox<String> stateFilter;
    private final ComboBox<String> cityFilter;
    private final ComboBox<String> phaseFilter;
    private final ComboBox<PropertyType> propertyTypeFilter;
    private final ComboBox<PropertyStatus> propertyStatusFilter;
    private final ComboBox<String> agentFilter;
    private final ComboBox<String> clientFilter;

    public GridTab(PropertyService propertyService, PhaseService phaseService, CityService cityService, StateService stateService, UserService userService) {
        this.propertyService = propertyService;
        this.phaseService = phaseService;
        this.cityService = cityService;
        this.stateService = stateService;
        this.userService = userService;

        setSpacing(true);
        setPadding(false);
        addClassName("properties-content");

        searchField = new TextField();
        searchField.setPlaceholder("Search Properties");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateGrid());
        searchField.addClassName("custom-search-field");
        searchField.addClassName("custom-toolbar-field");

        stateFilter = new ComboBox<>("State");
        stateFilter.setItems(stateService.getAllStates().stream().map(State::getName).collect(Collectors.toList()));
        stateFilter.addValueChangeListener(e -> onStateSelected());
        stateFilter.addClassName("custom-filter");

        cityFilter = new ComboBox<>("City");
        cityFilter.setEnabled(false);
        cityFilter.addValueChangeListener(e -> onCitySelected());
        cityFilter.addClassName("custom-filter");

        phaseFilter = new ComboBox<>("Phase");
        phaseFilter.setEnabled(false);
        phaseFilter.addValueChangeListener(e -> onPhaseSelected());
        phaseFilter.addClassName("custom-filter");

        propertyTypeFilter = new ComboBox<>("Type", PropertyType.values());
        propertyTypeFilter.addValueChangeListener(e -> updateGrid());
        propertyTypeFilter.addClassName("custom-filter");

        propertyStatusFilter = new ComboBox<>("Status", PropertyStatus.values());
        propertyStatusFilter.addValueChangeListener(e -> updateGrid());
        propertyStatusFilter.addClassName("custom-filter");

        agentFilter = new ComboBox<>("Agent");
        agentFilter.setItems(userService.getAgents().stream().map(agent -> agent.getFirstName() + " " + agent.getLastName()).collect(Collectors.toList()));
        agentFilter.addValueChangeListener(e -> updateGrid());
        agentFilter.addClassName("custom-filter");

        clientFilter = new ComboBox<>("Client");
        clientFilter.setItems(userService.getClients().stream().map(client -> client.getFirstName() + " " + client.getLastName()).collect(Collectors.toList()));
        clientFilter.addValueChangeListener(e -> updateGrid());
        clientFilter.addClassName("custom-filter");

        Button resetButton = new Button(new Icon(VaadinIcon.REFRESH));
        resetButton.addClickListener(e -> resetFilters());
        resetButton.addClassName("custom-button");
        resetButton.addClassName("custom-reset-button");
        resetButton.addClassName("custom-toolbar-button");

        Button addPropertyButton = new Button("Add Property");
        addPropertyButton.setPrefixComponent(new Icon(VaadinIcon.PLUS));
        addPropertyButton.addClickListener(e -> openAddPropertyDialog());
        addPropertyButton.addClassName("custom-button");
        addPropertyButton.addClassName("custom-add-button");
        addPropertyButton.addClassName("custom-toolbar-button");

        propertyGrid = new Grid<>(Property.class);
        propertyGrid.setColumns("street", "size");
//        propertyGrid.addComponentColumn(this::createImage).setHeader("Image").setWidth("50px").setTextAlign(ColumnTextAlign.START);
        propertyGrid.addColumn(property -> "₦" + NumberFormat.getNumberInstance(Locale.US).format(property.getPrice()))
                .setHeader("Price")
                .setKey("price")
                .setAutoWidth(true);
        propertyGrid.getStyle().setFontSize("14px");

        propertyGrid.addColumn(property -> property.getPropertyType().getDisplayName().replace("_", " "))
                .setHeader("Type")
                .setKey("propertyType")
                .setAutoWidth(true);

        propertyGrid.addColumn(property -> property.getPropertyStatus().name().replace("_", " "))
                .setHeader("Status")
                .setKey("propertyStatus")
                .setAutoWidth(true);

        propertyGrid.setItems(propertyService.getAllProperties());
        propertyGrid.addClassName("custom-grid");

        propertyStatusFilter.addValueChangeListener(event -> {
            PropertyStatus selectedStatus = event.getValue();
            if (selectedStatus != null && selectedStatus.equals(PropertyStatus.AVAILABLE)) {
                clientFilter.setVisible(false);
                agentFilter.setVisible(false);
            } else {
                agentFilter.setVisible(true);
                clientFilter.setVisible(true);
            }
        });

        propertyGrid.asSingleSelect().addValueChangeListener(event -> {
            Property selectedProperty = event.getValue();
            if (selectedProperty != null) {
                openEditPropertyDialog(selectedProperty);
            }
        });

        HorizontalLayout firstRowToolbar = new HorizontalLayout(searchField, stateFilter, cityFilter, phaseFilter, propertyTypeFilter, propertyStatusFilter, agentFilter, clientFilter, resetButton, addPropertyButton);
        firstRowToolbar.addClassName("custom-toolbar");
//        firstRowToolbar.setWidthFull();
        firstRowToolbar.getStyle().setDisplay(Style.Display.FLEX).setFlexWrap(Style.FlexWrap.WRAP);
        firstRowToolbar.getStyle().setAlignItems(Style.AlignItems.BASELINE);


        add(firstRowToolbar, propertyGrid);

        updateGrid();
    }


    private void updateGrid() {
        String keyword = searchField.getValue();
        String selectedState = stateFilter.getValue();
        String selectedCity = cityFilter.getValue();
        String selectedPhase = phaseFilter.getValue();
        PropertyType selectedPropertyType = propertyTypeFilter.getValue();
        PropertyStatus selectedPropertyStatus = propertyStatusFilter.getValue();
        String selectedAgent = agentFilter.getValue();
        String selectedClient = clientFilter.getValue();

        List<Property> properties = propertyService.searchPropertiesByFilters(keyword, selectedState, selectedCity, selectedPhase, selectedPropertyType, selectedPropertyStatus, selectedAgent, selectedClient);
        propertyGrid.setItems(properties);
    }


    private void resetFilters() {
        searchField.clear();
        stateFilter.clear();
        cityFilter.clear();
        phaseFilter.clear();
        propertyTypeFilter.clear();
        propertyStatusFilter.clear();
        agentFilter.clear();
        clientFilter.clear();
        updateGrid();
    }

    private void onStateSelected() {
        String selectedState = stateFilter.getValue();
        if (selectedState != null) {
            cityFilter.setItems(cityService.getCitiesByState(selectedState).stream().map(City::getName).collect(Collectors.toList()));
            cityFilter.setEnabled(true);
        } else {
            cityFilter.clear();
            cityFilter.setEnabled(false);
        }
        updateGrid();
    }

    private void onCitySelected() {
        String selectedCity = cityFilter.getValue();
        if (selectedCity != null) {
            phaseFilter.setItems(phaseService.getPhasesByCity(selectedCity).stream().map(Phase::getName).collect(Collectors.toList()));
            phaseFilter.setEnabled(true);
        } else {
            phaseFilter.clear();
            phaseFilter.setEnabled(false);
        }
        updateGrid();
    }

    private void onPhaseSelected(){
        String selectedPhase = phaseFilter.getValue();
        if (selectedPhase!= null) {
            phaseFilter.setValue(selectedPhase);
            phaseFilter.setEnabled(true);
        } else {
            phaseFilter.clear();
            phaseFilter.setEnabled(false);
        }
        updateGrid();
    }

    private void openAddPropertyDialog() {
        AddPropertyForm addPropertyForm = new AddPropertyForm(propertyService, phaseService, userService, (v) -> updateGrid());
        addPropertyForm.open();
    }

    private void openEditPropertyDialog(Property property) {
        EditPropertyForm editPropertyForm = new EditPropertyForm(propertyService, phaseService, userService, property, (v) -> updateGrid());
        editPropertyForm.open();
    }

    private Image createImage(Property property) {
        if (property.getPropertyImages() != null && !property.getPropertyImages().isEmpty()) {
            byte[] imageBytes = property.getPropertyImages().get(0).getPropertyImages();
            StreamResource resource = new StreamResource("property-image-" + property.getId(), () -> new ByteArrayInputStream(imageBytes));
            Image image = new Image(resource, "Property Image");
            image.setMaxHeight("100px");
            image.setMaxWidth("50px");
            return image;
        }
        return new Image("placeholder-image-url", "No Image");
    }
}