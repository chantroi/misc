const uploadButton = document.querySelector('#upload-btn');
const popupForm = document.getElementById('popup-form');

function onPlay(ele) {
    const name = ele.textContent;
    const imageExt = ['.jpeg', '.png', '.jpg'];
    let playLink;
    let item;
    fetch(`/s3/get?q=${name}`)
      .then(response => response.json())
      .then(data => {
          playLink = data.result;
      });
    
    if (name.endsWith('.mp4')) {
        item = `<video href="${playLink}" controls></video>`;
    }
    if (imageExt.some(ext => name.endsWith(ext))) {
        item = `<image src="${playLink}"></image>`;
    }
   if (name.endsWith('.mp3')) {
       item = `<audio src="${playLink}" controls></audio>`;
   }
   const popup = ```
        <div id="popup">
          ${item}
        </div>
    ```;
   document.body.appendChild(popup);
}

function submitForm(event) {
    event.preventDefault();
    this.parentNode.submit();
}

function handleClick(event) {
    popupForm.style.display = "";
}

function closePopup(event) {
    popupForm.style.display = "none";
}

uploadButton.addEventListener('click', handleClick);