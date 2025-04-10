# 🎴 Fehlkauf Wizard
**Ein kleines Tool, das die Adressvergabe für die Fehlkauf-Runde in der Postcrossing-Community automatisiert – weil manuelle Zuordnung von 400+ Karten an 50+ Leute doch etwas... optimierungswürdig ist.**  


## 🔧 Was es kann
- 📊 **Verteilt Adressen** für die aktuelle Fehlkauf-Runde  
- ♾️ **Maximiert die Kartenanzahl** für User mit "max"-Angabe  
- ✨ **Generiert fertige Listen** im Forum-Format (kein manuelles Kopieren!)  
- 📂 **Output-Dateien**:
  - `Fehlkauf<NR>-liste.txt` – Übersicht für dich zum abhaken 
  - `Fehlkauf<NR>-forum.txt` – Copy-Paste-ready fürs Forum  
  - `Fehlkauf<NR>-receivers.txt` – Abhaklisten für die Users
  - `Fehlkauf<NR>-übersicht.txt` – Wer schickt wie viel?
  - `Fehlkauf<NR>-message-receivers.txt` – Vorbereitete Usernamen für die Einladungs-PMs
    
## 👩‍💻 Build
``` bash
mvn package -Pcli
mvn package -Pgui
```

## 🚀 Schnellstart
### 1. **CSV vorbereiten** (Beispiel-Format siehe unten) 
→ ab nach `<PFAD_ZUM_ORDNER>/Fehlkauf<NR>.csv`  
### 2. **Starten**
 
  1. Grafische Oberfläche (einfach)  
Einfach die `FehlkaufWizard.exe` oder `FehlkaufWizard-jar-with-dependencies.jar` doppelklicken!

  2. Kommandozeile:  
   ```bash
   java -jar target/FehlkaufMatcher.jar <PFAD_ZUM_ORDNER> <RUNDENNUMMER>
```

### 3. **Ergebnisse checken** 
Neue .txt-Dateien erscheinen im Ordner, in dem auch die CSV-Datei liegt (<PFAD_ZUM_ORDNER>)!

## 📝 CSV-Format
Deine Eingabedatei (Fehlkauf<NR>.csv) sollte so aussehen:
```
miau123;Mieze Katz
Miau Str. 12
12345 Berlin;3
purrrrr;Purry Gripp
Purrweg 5
65432 Frankfurt;max
```

# 📮 Happy Postcrossing!
