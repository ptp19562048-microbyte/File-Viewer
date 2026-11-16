# File Viewer

File Viewer este o aplicație web responsivă, în limba română, care citește fișiere local în browser și oferă previzualizări pentru formatele uzuale.

## Funcții

- Selectare de fișiere pe telefon sau desktop; acceptă orice extensie.
- Previzualizare locală pentru text/cod (inclusiv JSON, CSV și Markdown), imagini, PDF, audio și video.
- Pentru formatele nesuportate: detalii despre fișier, descărcare și partajare prin foaia de partajare a sistemului, dacă browserul o permite.
- APK-urile sunt recunoscute și explicate în siguranță; aplicația nu le execută și nu le instalează.
- Fișierele nu sunt trimise către un server. Previzualizarea textului este limitată la 2 MB.

## Rulare

Deschide `index.html` într-un browser modern sau publică cele trei fișiere în orice gazdă statică HTTPS, de exemplu GitHub Pages.

## Limite importante

Nu există o aplicație care să poată interpreta orice format fără un viewer corespunzător. Tipurile necunoscute pot fi descărcate ori trimise unei aplicații deja instalate. APK-urile sunt pachete Android și nu pot fi instalate pe iOS/iPadOS; instalarea pe Android este controlată de sistemul de operare și necesită aprobarea utilizatorului. Comportamentul de previzualizare depinde și de suportul browserului și al dispozitivului.
