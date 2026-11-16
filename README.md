# File Viewer — versiune web + Android

În acest repository există acum două variante: 

- versiunea web, în fișierele principale din rădăcina repo-ului (`index.html`, `app.js`, `styles.css`)
- versiunea Android, într-un proiect separat din `android-app/`

## Versiunea web

Deschide `index.html` într-un browser modern. Aceasta permite:

- selectarea unui fișier din telefon sau desktop
- previzualizare locală pentru text, imagini, PDF, audio/video
- detectare de fișiere APK și informații despre ele
- partajare/descărcare pentru formatele necunoscute

## Versiunea Android

Proiectul din `android-app/` este un starter Android simplu care:

- alege orice fișier cu `ACTION_OPEN_DOCUMENT`
- afișează informații despre fișier
- previzualizează text și imagini
- poate partaja sau deschide fișierul cu altă aplicație
- detectează APK-uri și explică că nu pot fi instalate direct din aplicație

## Important

Nu există aplicații care să deschidă “oricare” fișier fără să apeleze un viewer compatibil. Browserul, Android și iOS depind de sistemul de operare și de aplicațiile instalate. APK-urile nu pot fi instalate direct din browser pe iPhone/iPad și nu trebuie instalate dintr-o aplicație web fără aprobarea utilizatorului.
