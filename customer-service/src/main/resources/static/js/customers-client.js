async function loadCustomers() {

    const status = document.getElementById("status");
    const table = document.getElementById("customerTable");
    const tableBody = document.getElementById("customerTableBody");

    try {
        const response = await fetch("/api/v1/customers");

        if (!response.ok) {
            throw new Error(
                "Unable to load customers. HTTP " + response.status
            );
        }

        const customers = await response.json();

        // clear any existing rows
        tableBody.innerHTML = "";

        if (customers.length === 0) {
            status.textContent = "No customers found.";
            table.hidden = true;
            return;
        }

        customers.forEach(customer => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${customer.id}</td>
                <td>${escapeHtml(customer.name)}</td>
                <td>${escapeHtml(customer.contactName ?? "")}</td>
                <td>${escapeHtml(customer.phone ?? "")}</td>
                <td>${customer.active ? "Yes" : "No"}</td>
                <td>
                    <a href="/customer-edit-client.html?id=${customer.id}">
                        Edit
                    </a>
                </td>
            `;

            tableBody.appendChild(row);
        });

        status.textContent = "";
        table.hidden = false;

    } catch (error) {
        table.hidden = true;
        status.textContent = error.message;
    }
}

function escapeHtml(value) {
    const element = document.createElement("div");
    element.textContent = value;
    return element.innerHTML;
}

loadCustomers();