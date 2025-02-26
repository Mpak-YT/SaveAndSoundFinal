const searchInput = document.getElementById("searchInput");
const searchButton = document.getElementById("searchButton");

searchInput.addEventListener('input', function (){
    if (searchInput.value.trim() === ''){
        searchButton.disabled = true;
    }
    else {
        searchButton.disabled = false;
    }
})