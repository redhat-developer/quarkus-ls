package com.redhat.qute.parser.injection;

import com.redhat.qute.parser.scanner.MultiLineStream;

public interface InjectionDetector {
    /**
     * Détecte si une injection commence à la position courante du stream
     * 
     * @param stream le stream à la position courante
     * @return les métadonnées de l'injection, ou null si aucune injection
     */
    InjectionMetadata detectInjection(MultiLineStream stream);
    
    /**
     * Trouve la fin de l'injection et avance le stream jusqu'à cette position
     * 
     * @param stream le stream positionné au début du contenu de l'injection
     * @return true si la fin a été trouvée, false sinon
     */
    boolean scanToInjectionEnd(MultiLineStream stream);
    
    /**
     * Scanne le délimiteur de début de l'injection
     * 
     * @param stream le stream à la position courante
     * @return true si le délimiteur a été scanné avec succès
     */
    boolean scanStartDelimiter(MultiLineStream stream);
    
    /**
     * Scanne le délimiteur de fin de l'injection
     * 
     * @param stream le stream à la position courante
     * @return true si le délimiteur a été scanné avec succès
     */
    boolean scanEndDelimiter(MultiLineStream stream);
}
