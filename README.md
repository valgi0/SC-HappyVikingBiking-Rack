# SC-HappyVikingBiking-Rack
Progetto per l'esame di Smart City e Tecnologie mobili, AA 2018-2019.

## Guida all'utilizzo

Il jar eseguibile può essere generato via task shadowJar di Gradle
Sono disponibili diversi paramateri per la configurazione iniziale della rastrelliera, specificabili in fase di avvio del Jar: 

* --remoteaddress:Specifica l'ip remoto del server da contattare
* --remoteport:Specifica la porta remota del server da contattare
* --serverport: Specifica la porta su cui il server della rack sarà messo in esecuzione
* --serveraddress: Specifica l'indirizzo IP su cui deve essere esposto il server
* --rackname: Specifica il nome della rastrelliera
* --ip_brackets_pinlist: permette di configurare lo stato iniziale della rastrelliera, la stringa va formattata secondo la seguente sintassi:
 SourceIP1 : ButtonPin1, BlockLed1, FreeLed1 : BikeLockedID(if bike is present, if not leave it empty);
  SourceIP2 : ButtonPin2, BlockLed2, BlockLed2 ;...
