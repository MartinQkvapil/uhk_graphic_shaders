První úloha: zobrazení prostorové scény obsahující geometrického tělesa definované pomocí rovinného gridu trojúhelníků, s mapováním textury na povrchu, s osvětlením a vrženými stíny
1. [done] Prostudujte si ukázky programování shaderů v gitovém repositáři.
2. [done] Vytvořte vertex a index buffer pro uložení geometrie tělesa založené na síti trojúhelníků – grid.
3. [done] Vyzkoušejte implementaci gridu pomoci seznamu (list) i pásu trojúhelníků (strip). Model zobrazte ve formě bodů, hran i vyplněných ploch.
4. [done] Shaderovým programem ve vertex shaderu modifikujte tvar rovinného gridu na prostorové těleso.
5. [done] Implementujte alespoň 6 funkcí, dvě v kartézských, dvě ve sférických a dvě v cylindrických souřadnicích. 
Vždy jedna může být použita z uvedených stránek a druhou „pěknou“ navrhněte. 
Výpočet geometrie zobrazovaných těles (souřadnic vrcholů) i přepočet na zvolený souřadnicový systém bude prováděn vertexovým programem! 
Vstupem do GPU bude vždy rovinný grid. 
6. [done] Alespoň jednu z funkcí modifikujte v čase pomocí uniform proměnné. (time)
7. [done] Zobrazte alespoň dvě tělesa zároveň, vypočtené a načtené.
8. Vytvořte vhodné pixelové programy pro renderování hodnot na povrchu těles znázorňující barevně pozici (souřadnici xyz, hloubku), barvu povrchu, mapovanou texturu, normálu a souřadnice do textury, vzdálenost od zdroje světla, vhodné pro debugování.
9. [done] Normálu povrchu určete parciální derivací funkce nebo diferencí ve vertex shaderu.
10. [done] Umožněte změnu modelovací transformace tělesa, animací v čase nebo ovládáním uživatelem.
11. [done] Pozorovatele (pohledovou transformaci) nastavte pomocí kamery a ovládejte myší (rozhlížení) a klávesami WSAD (pohyb vpřed, vzad, vlevo, vpravo).
12. [done] Umožněte přepínání ortogonální i perspektivní projekce.
13. Vytvořte pixelový program pro zobrazení texturovaného osvětleného povrchu pomoci Blinn-Phong osvětlovacího modelu, všechny složky. Jednotlivé složky samostatně zapínejte a umožněte změnu vzájemné polohy světla a tělesa.
14. Implementujte reflektorový zdroj světla a útlum prostředí. Implementujte hladký přechod na okraji reflektorového světla. Pozici zdroje světla znázorněte.
15. [done] Vyplňte autoevaluační tabulku k sebehodnocení odevzdaného řešení. Tabulku odevzdejte jako součást odevzdání úlohy v adresáři projektu. Slouží především k shrnutí vámi implementované funkcionality projektu a k zjednodušení hodnocení.
16. Implementujte jeden postprocessingový průchod vizualizačním řetězcem, který upraví výsledek rendrování prostorové scény vypočtený po prvním průchodu. Využijte RenderTarget pro kreslení do rastrového obrazu, který následně zpracujte v druhém průchodu. Zvolte vhodnou rastrovou operaci, např. rozmazání, zaostření, změnu barev a podobně, a implementujte ji jako fragment shaderovou operaci v samostatném průchodu pipelinou.
17. [done] V rámci řešení první průběžné úlohy i semestrálního projektu bude požadováno, aby si studenti verzovali (pomocí gitlab.com nebo github.com) postup vývoje aplikace. Vytvořte si privátní repozitář, přidejte vašeho cvičícího a pravidelně ukládejte jednotlivé commity. Finální verzi projektu odevzdejte standardně prostřednictvím Olivy.
18. Před odevzdáním si znovu přečtěte pravidla odevzdávání a hodnocení projektů uvedené v Průvodci studiem
