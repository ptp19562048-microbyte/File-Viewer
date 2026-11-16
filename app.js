const input = document.querySelector('#file-input');
const dropzone = document.querySelector('#dropzone');
const results = document.querySelector('#results');
const preview = document.querySelector('#preview');
const nameHeading = document.querySelector('#file-name-heading');
const nameElement = document.querySelector('#file-name');
const metaElement = document.querySelector('#file-meta');
const badge = document.querySelector('#file-badge');
const downloadButton = document.querySelector('#download-button');
const shareButton = document.querySelector('#share-button');
const statusMessage = document.querySelector('#status-message');
const maxTextPreviewBytes = 2 * 1024 * 1024;
let activeUrl = null;
let currentFile = null;

const textExtensions = new Set([
  'txt', 'text', 'md', 'markdown', 'json', 'csv', 'tsv', 'xml', 'html', 'htm',
  'css', 'js', 'mjs', 'cjs', 'ts', 'jsx', 'tsx', 'py', 'java', 'kt', 'c', 'h',
  'cpp', 'hpp', 'cs', 'go', 'rs', 'php', 'rb', 'sh', 'bash', 'yml', 'yaml',
  'toml', 'ini', 'conf', 'log', 'sql', 'svg', 'env'
]);

function extensionOf(name) {
  const dot = name.lastIndexOf('.');
  return dot > 0 ? name.slice(dot + 1).toLowerCase() : '';
}

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  const units = ['KB', 'MB', 'GB', 'TB'];
  let value = bytes / 1024;
  let index = 0;
  while (value >= 1024 && index < units.length - 1) {
    value /= 1024;
    index += 1;
  }
  return `${value.toLocaleString('ro-RO', { maximumFractionDigits: 1 })} ${units[index]}`;
}

function makeElement(tag, className, text) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined) element.textContent = text;
  return element;
}

function resetPreview() {
  preview.replaceChildren();
  statusMessage.textContent = '';
  if (activeUrl) URL.revokeObjectURL(activeUrl);
  activeUrl = null;
}

function showMessage(title, detail) {
  const box = makeElement('div', 'preview-message');
  box.append(makeElement('strong', '', title), makeElement('p', '', detail));
  preview.replaceChildren(box);
}

async function openFile(file) {
  if (!file) return;
  resetPreview();
  currentFile = file;
  activeUrl = URL.createObjectURL(file);
  const ext = extensionOf(file.name);
  const type = (file.type || '').toLowerCase();
  const isApk = ext === 'apk';
  const isImage = type.startsWith('image/') && ext !== 'svg';
  const isPdf = type === 'application/pdf' || ext === 'pdf';
  const isAudio = type.startsWith('audio/');
  const isVideo = type.startsWith('video/');
  const isText = textExtensions.has(ext) || type.startsWith('text/') || type === 'application/json';

  nameHeading.textContent = file.name;
  nameElement.textContent = file.name;
  metaElement.textContent = `${formatSize(file.size)}${type ? ` · ${type}` : ext ? ` · .${ext}` : ' · tip necunoscut'}`;
  badge.textContent = isApk ? 'APK' : (ext || 'FILE').slice(0, 5).toUpperCase();
  badge.classList.toggle('apk-badge', isApk);
  downloadButton.href = activeUrl;
  downloadButton.download = file.name;
  results.hidden = false;
  results.scrollIntoView({ behavior: 'smooth', block: 'start' });

  if (isImage) {
    const image = makeElement('img', 'media-preview');
    image.alt = `Previzualizare pentru ${file.name}`;
    image.src = activeUrl;
    preview.replaceChildren(image);
    return;
  }
  if (isPdf) {
    const frame = makeElement('iframe', 'pdf-preview');
    frame.title = `Previzualizare PDF: ${file.name}`;
    frame.src = activeUrl;
    preview.replaceChildren(frame);
    return;
  }
  if (isAudio || isVideo) {
    const media = makeElement(isAudio ? 'audio' : 'video', 'media-player');
    media.controls = true;
    media.src = activeUrl;
    if (isVideo) media.playsInline = true;
    preview.replaceChildren(media);
    return;
  }
  if (isText) {
    if (file.size > maxTextPreviewBytes) {
      showMessage('Fișierul este prea mare pentru previzualizare', 'Previzualizarea textului este limitată la 2 MB. Îl poți descărca sau trimite către o altă aplicație.');
      return;
    }
    try {
      const content = await file.text();
      const code = makeElement('pre', 'text-preview');
      const rendered = makeElement('code', '', content);
      if (ext === 'json') {
        try { rendered.textContent = JSON.stringify(JSON.parse(content), null, 2); } catch { /* Păstrează textul original dacă JSON-ul nu este valid. */ }
      }
      code.append(rendered);
      preview.replaceChildren(code);
    } catch {
      showMessage('Nu s-a putut citi fișierul', 'Încearcă să-l descarci sau să-l deschizi cu o aplicație compatibilă.');
    }
    return;
  }
  if (isApk) {
    showMessage('Fișier APK detectat', 'Acesta este un pachet de aplicație Android. Browserul nu îl execută și nu îl instalează. Descarcă-l și, dacă ai încredere în sursă, deschide-l pe un dispozitiv Android; instalarea poate necesita permisiunea „Instalare aplicații necunoscute”. Pe iPhone/iPad, APK-urile nu sunt compatibile.');
    return;
  }
  showMessage('Previzualizarea nu este disponibilă', 'Formatul poate fi deschis de o aplicație instalată pe dispozitiv. Folosește „Trimite / Deschide” sau descarcă fișierul.');
}

function handleFiles(files) {
  if (files && files.length) openFile(files[0]);
}

input.addEventListener('change', (event) => handleFiles(event.target.files));

dropzone.addEventListener('keydown', (event) => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    input.click();
  }
});

for (const eventName of ['dragenter', 'dragover']) {
  dropzone.addEventListener(eventName, (event) => {
    event.preventDefault();
    dropzone.classList.add('is-dragging');
  });
}
for (const eventName of ['dragleave', 'drop']) {
  dropzone.addEventListener(eventName, (event) => {
    event.preventDefault();
    dropzone.classList.remove('is-dragging');
  });
}
dropzone.addEventListener('drop', (event) => handleFiles(event.dataTransfer.files));

document.querySelector('#clear-button').addEventListener('click', () => {
  resetPreview();
  currentFile = null;
  input.value = '';
  results.hidden = true;
  dropzone.focus();
});

shareButton.addEventListener('click', async () => {
  if (!currentFile) return;
  if (navigator.canShare && navigator.canShare({ files: [currentFile] }) && navigator.share) {
    try {
      await navigator.share({ files: [currentFile], title: currentFile.name });
      statusMessage.textContent = 'Fișierul a fost trimis.';
    } catch (error) {
      if (error.name !== 'AbortError') statusMessage.textContent = 'Partajarea nu a fost disponibilă. Încearcă să descarci fișierul.';
    }
  } else {
    statusMessage.textContent = 'Partajarea directă nu este disponibilă în acest browser. Descarcă fișierul pentru a-l deschide cu o altă aplicație.';
  }
});

window.addEventListener('beforeunload', () => {
  if (activeUrl) URL.revokeObjectURL(activeUrl);
});
