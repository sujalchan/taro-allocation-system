const allocationForm = document.getElementById("allocationForm");
const customerSelect = document.getElementById("customerId");
const weekStartInput = document.getElementById("weekStart");
const allocationItemsContainer = document.getElementById("allocationItems");
const addItemButton = document.getElementById("addItemButton");
const errorContainer = document.getElementById("errorContainer");
const errorList = document.getElementById("errorList");

let taroTypes = [];

loadReferenceData();

addItemButton.addEventListener("click", addAllocationItem);
allocationForm.addEventListener("submit", createAllocation);

allocationItemsContainer.addEventListener("click", event => {
    if (event.target.classList.contains("remove-item-button")) {
        removeAllocationItem(event.target);
    }
});

async function loadReferenceData() {
    clearErrors();

    try {
        const [customerResponse, taroResponse] = await Promise.all([
            fetch("/api/v1/customer-service-reference/customers"),
            fetch("/api/v1/customer-service-reference/taro-types")
        ]);

        if (!customerResponse.ok || !taroResponse.ok) {
            showError("Unable to load customer or taro type data.");
            return;
        }

        const customers = await customerResponse.json();
        taroTypes = await taroResponse.json();

        populateCustomers(customers);
        populateAllTaroTypeSelects();
    } catch (error) {
        showError("Unable to load customer or taro type data.");
    }
}

function populateCustomers(customers) {
    customerSelect.innerHTML = '<option value="">Select Customer</option>';

    customers.forEach(customer => {
        const option = document.createElement("option");
        option.value = customer.id;
        option.textContent = customer.name;
        customerSelect.appendChild(option);
    });
}

function populateAllTaroTypeSelects() {
    document.querySelectorAll(".taro-type-select").forEach(select => {
        populateTaroTypeSelect(select);
    });
}

function populateTaroTypeSelect(select) {
    select.innerHTML = '<option value="">Select Taro Type</option>';

    taroTypes.forEach(taroType => {
        const option = document.createElement("option");
        option.value = taroType.id;
        option.textContent = taroType.name;
        select.appendChild(option);
    });
}

function addAllocationItem() {
    const firstRow = allocationItemsContainer.querySelector(".allocation-item-row");
    const newRow = firstRow.cloneNode(true);

    const taroSelect = newRow.querySelector(".taro-type-select");
    const quantityInput = newRow.querySelector(".quantity-input");
    const priceInput = newRow.querySelector(".price-input");

    taroSelect.value = "";
    quantityInput.value = "";
    priceInput.value = "";

    allocationItemsContainer.appendChild(newRow);
}

function removeAllocationItem(button) {
    const rows = allocationItemsContainer.querySelectorAll(".allocation-item-row");

    // keep at least one allocation item
    if (rows.length <= 1) {
        return;
    }

    button.closest(".allocation-item-row").remove();
}

async function createAllocation(event) {
    event.preventDefault();
    clearErrors();

    const rows = allocationItemsContainer.querySelectorAll(".allocation-item-row");
    const allocationItems = [];

    rows.forEach(row => {
        const taroTypeId = row.querySelector(".taro-type-select").value;
        const quantity = row.querySelector(".quantity-input").value;
        const price = row.querySelector(".price-input").value;

        const item = {
            taroTypeId: Number(taroTypeId),
            quantity: Number(quantity)
        };

        if (price !== "") {
            item.pricePerKg = Number(price);
        }

        allocationItems.push(item);
    });

    const requestBody = {
        customerId: Number(customerSelect.value),
        weekStart: weekStartInput.value,
        allocationItems: allocationItems
    };

    try {
        const response = await fetch("/api/v1/allocations", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            const error = await readError(response);
            showError(error.message);
            return;
        }

        window.location.href = "/allocations-client.html";
    } catch (error) {
        showError("Unable to connect to the allocation service.");
    }
}

async function readError(response) {
    try {
        return await response.json();
    } catch (error) {
        return {
            message: "An unexpected error occurred."
        };
    }
}

function showError(message) {
    errorContainer.hidden = false;

    const item = document.createElement("li");
    item.textContent = message;

    errorList.appendChild(item);
}

function clearErrors() {
    errorContainer.hidden = true;
    errorList.innerHTML = "";
}