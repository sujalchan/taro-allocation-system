const allocationForm = document.getElementById("allocationForm");
const customerSelect = document.getElementById("customerId");
const weekStartInput = document.getElementById("weekStart");
const allocationItemsContainer = document.getElementById("allocationItems");
const addItemButton = document.getElementById("addItemButton");
const errorContainer = document.getElementById("errorContainer");
const errorList = document.getElementById("errorList");

const params = new URLSearchParams(window.location.search);
const allocationId = params.get("id");

let taroTypes = [];

loadPage();

addItemButton.addEventListener("click", () => addAllocationItem());
allocationForm.addEventListener("submit", updateAllocation);

allocationItemsContainer.addEventListener("click", event => {
    if (event.target.classList.contains("remove-item-button")) {
        removeAllocationItem(event.target);
    }
});

async function loadPage() {
    clearErrors();

    if (!allocationId) {
        showError("Allocation ID is missing.");
        allocationForm.hidden = true;
        return;
    }

    try {
        const [allocationResponse, customerResponse, taroResponse] = await Promise.all([
            fetch(`/api/v1/allocations/${allocationId}`),
            fetch("/api/v1/customer-service-reference/customers"),
            fetch("/api/v1/customer-service-reference/taro-types")
        ]);

        if (!allocationResponse.ok) {
            const error = await readError(allocationResponse);
            showError(error.message);
            allocationForm.hidden = true;
            return;
        }

        if (!customerResponse.ok || !taroResponse.ok) {
            showError("Unable to load customer or taro type data.");
            allocationForm.hidden = true;
            return;
        }

        const allocation = await allocationResponse.json();
        const customers = await customerResponse.json();
        taroTypes = await taroResponse.json();

        populateCustomers(customers, allocation.customerId);
        weekStartInput.value = allocation.weekStart;

        allocation.allocationItems.forEach(item => {
            addAllocationItem(item);
        });
    } catch (error) {
        console.error(error);
        showError("Unable to load allocation data.");
        allocationForm.hidden = true;
    }
}

function populateCustomers(customers, selectedCustomerId) {
    customerSelect.innerHTML = '<option value="">Select Customer</option>';

    customers.forEach(customer => {
        const option = document.createElement("option");
        option.value = customer.id;
        option.textContent = customer.name;

        if (customer.id === selectedCustomerId) {
            option.selected = true;
        }

        customerSelect.appendChild(option);
    });
}

function populateTaroTypeSelect(select, selectedTaroTypeId = null) {
    select.innerHTML = '<option value="">Select Taro Type</option>';

    taroTypes.forEach(taroType => {
        const option = document.createElement("option");
        option.value = taroType.id;
        option.textContent = taroType.name;

        if (taroType.id === selectedTaroTypeId) {
            option.selected = true;
        }

        select.appendChild(option);
    });
}

function addAllocationItem(existingItem = null) {
    const row = document.createElement("div");
    row.className = "allocation-item-row";

    row.innerHTML = `
        <div>
            <label>Taro Type</label>
            <select class="taro-type-select" required></select>
        </div>

        <div>
            <label>Quantity</label>
            <input type="number" class="quantity-input" step="1" min="1" required>
        </div>

        <div>
            <label>Price per kg</label>
            <input type="number" class="price-input" step="0.01" min="0">

            <small>
                Leave blank to use the standard price.
            </small>
        </div>

        <div>
            <button type="button" class="remove-item-button">
                Remove
            </button>
        </div>

        <br>
    `;

    const taroSelect = row.querySelector(".taro-type-select");
    const quantityInput = row.querySelector(".quantity-input");
    const priceInput = row.querySelector(".price-input");

    populateTaroTypeSelect(
        taroSelect,
        existingItem ? existingItem.taroTypeId : null
    );

    if (existingItem) {
        quantityInput.value = existingItem.quantity;
        priceInput.value = existingItem.pricePerKg;
    }

    allocationItemsContainer.appendChild(row);
}

function removeAllocationItem(button) {
    const rows = allocationItemsContainer.querySelectorAll(".allocation-item-row");

    // keep at least one allocation item
    if (rows.length <= 1) {
        return;
    }

    button.closest(".allocation-item-row").remove();
}

async function updateAllocation(event) {
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
        const response = await fetch(`/api/v1/allocations/${allocationId}`, {
            method: "PUT",
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