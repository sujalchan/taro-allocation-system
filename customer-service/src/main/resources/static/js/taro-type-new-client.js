const form = document.getElementById("taroTypeForm");

form.addEventListener("submit", async function (event) {
    event.preventDefault();

    clearErrors();

    const taroType = {
        name: document.getElementById("name").value,
        description: document.getElementById("description").value,
        standardPrice: document.getElementById("standardPrice").value
    };

    try {
        const response = await fetch("/api/v1/taro-types", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(taroType)
        });

        if (!response.ok) {
            const error = await response.json();
            showError(error.message);
            return;
        }

        window.location.href = "/taro-types-client.html";

    } catch (error) {
        showError("Unable to communicate with the server.");
    }
});

function showError(message) {
    const errorContainer =
        document.getElementById("errorContainer");

    const errorList =
        document.getElementById("errorList");

    const item = document.createElement("li");
    item.textContent = message;

    errorList.appendChild(item);
    errorContainer.hidden = false;
}

function clearErrors() {
    document.getElementById("errorList").innerHTML = "";

    document.getElementById("errorContainer").hidden = true;
}