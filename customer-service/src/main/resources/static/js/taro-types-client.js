async function loadTaroTypes() {

    const status = document.getElementById("status");
    const table = document.getElementById("taroTypeTable");
    const tableBody = document.getElementById("taroTypeTableBody");

    try {
        const response = await fetch("/api/v1/taro-types");

        if (!response.ok) {
            throw new Error(
                "Unable to load taro types. HTTP " + response.status
            );
        }

        const taroTypes = await response.json();

        // clear any existing rows
        tableBody.innerHTML = "";

        if (taroTypes.length === 0) {
            status.textContent = "No taro types found.";
            table.hidden = true;
            return;
        }

        taroTypes.forEach(taroType => {
            const row = document.createElement("tr");

            const price = Number(taroType.standardPrice)
                .toFixed(2);

            row.innerHTML = `
                        <td>${taroType.id}</td>
                        <td>${escapeHtml(taroType.name)}</td>
                        <td>${escapeHtml(taroType.description ?? "")}</td>
                        <td>$${price}</td>
                        <td>
                            <a href="/taro-type-edit-client.html?id=${taroType.id}">
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

loadTaroTypes();