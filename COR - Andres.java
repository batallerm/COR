//run("Open...");
getPixelSize(unit, pw, ph);

if(unit == "inches"){
	setVoxelSize(pw * 25.4, ph * 25.4,0 , "mm");
}
else if(unit == "mm"){
	print("");
}
else{
	print("La unidad de medida es: " + unit + ". No esta en la configuracion" );
}

//¡¡DATO IMPORTANTE PROGRAMACIÓN!! -> La matriz coge la imagen de cada píxel en el centro del píxel. ImageJ, al analizar los valores de gris de la matriz, asigna para cada pixel un valor en mm. El valor en mm escogido es el que correspondería al inicio del píxel y no al medio del píxel. Por tanto, hay una desviación de medio píxel a la izquierda (o hacia el cero). Luego, al pasar de nuevo estos mm a píxeles, se sigue desviando medio pixel.
//EJEMPLO: tenemos una matriz de 5 pixeles y 10 mm. La posición del valor de gris del primer píxel debería caer en 1 mm y luego, al volver a pasar a píxeles, caer en 0.5 píxeles de distancia. Sin embargo, ImageJ dice que la posicion del valor de gris del primer píxel cae en 0 mm y, por tanto, al pasar esta distancia a píxeles, cae en 0 píxeles de distancia. Se desvía sistemáticamente medio píxel hacia la izquierda.
//SOLUCIÓN: se añade manualmente 0.5 píxeles a los resultados obtenidos (cuando calculamos los parámetros "posx" y "posy").
print ("Buenos dias.");
print("Para leer la descripcion pulse Esc y agrande la ventana.");
print("Script realizado por Andres Bataller Marti - R1 de Radiofisica del Consorcio Hospitalario Provincial de Castellon");
print("");
print("Este script analiza el Centro de Rotacion (COR) utilizando 3 fuentes. Tambien puede usarse utilizando 1 fuente o 2.");
print("Los valores de las tolerancias que se dan son las sugeridas en el Protocolo de control de calidad de la instrumentacion de Medicina Nuclear version 2020 redactado por la SEFM, la SEMNIM y la SEPR.");
print("Para que el script funcione solicita que se inserten unos valores. Estos se sugieren de forma automatica y, en principio, no es necesario modificarlos.");
print("");
print("Para analizar las imagenes debe calcular el punto en el que se proyecta la fuente en el detector para cada angulacion. Para esto se realiza un ajuste a una gaussiana y se toma como posicion el punto de maximo valor de gris tras el ajuste");
print(""); 
print("Primero ofrece el valor medio de Y. Indica la posicion media en Y que ocupa para cada fuente. Hay que apuntar este valor ya que este valor debe coincidir con el del otro detector con una tolerancia de 0.5 pixeles.");
print("Tambien indica la desviacion (media y maxima) de cada medida de Y con respecto a su valor medio. Nos indica el pandeo del SPECT. Si el SPECT fuese perfecto esta desviacion deberia ser de 0. La tolerancia de la desviacion media es de 0.5 pixeles");
print("");
print("A continuacion se analiza el COR mediante 2 metodos distintos. Ambos metodos nos tienen que dar un valor muy parecido.");
print("El valor del COR es el punto alrededor del cual giran los detectores. El COR debe coincidir con el centro de la matriz de deteccion con una tolerancia de 0.5 pixeles.");
print("El primer valor del COR viene dado por un calculo del centro de masas. Analiza las posiciones de X para cada imagen y calcula la media.");
print("El segundo valor del COR se obtiene mediante un ajuste a un seno. El valor del desplazamiento del origen nos da el valor del COR.");
print("");
print("El COR en realidad no es un punto, es como una circunferencia deforme cuyo borde se aleja mas o menos del punto ideal en funcion del angulo de los detectores.");
print("El script analiza la desviacion del ajuste del seno para cada angulo del detector. Indica cuanto se desvia, en funcion del angulo, respecto del punto anteriormente calculado.");
print("No puede haber ningun punto que se desvie mas de 1 pixel y la media de desviaciones no puede ser mayor de 0.5 pixeles. Esto nos asegura que la circunferencia del COR no es muy grande ni deforme.");
print("");
print("Finalmente el script nos da los valores del COR para las 3 fuentes usando el metodo del ajuste al seno.");
print("La diferencia maxima entre estos valores debe de ser inferior a 0.5 pixeles.");
print("Podria usarse tambien el metodo del centro de masas; pero como ambos metodos dan valores muy parecidos se ha decidido usar solo uno de los dos metodos.");
print("");

ROI = 0;
//SOLICITO EL NÚMERO DE IMAGENES DE LA PRUEBA
Dialog.create("Numero imagenes");
Dialog.addMessage("Indique la imagen inicial y final");
Dialog.addNumber("imagen inicial: ", 1);
Dialog.addNumber("imagen final: ", 60);
Dialog.addMessage("Indique el tamanyo de la matriz en mm y en pixeles");
Dialog.addNumber("tamanyo matriz en mm: ", 565.84);
Dialog.addNumber("pixeles matriz: ", 256);
Dialog.show();
slice_inicial = Dialog.getNumber();
slice_final = Dialog.getNumber();
ROI_slice_max = slice_final - slice_inicial + 1;
matrizmm = Dialog.getNumber();
matrizpixeles = Dialog.getNumber();
print("numero total de imagenes " + ROI_slice_max + "");
print("tamanyo matriz mm " + matrizmm + "");
print("tamanyo matriz pixeles " + matrizpixeles + "");
setOption("ExpandableArrays", true);
xArray = newArray;
yArray = newArray;
slice = newArray;
//CORcomparacion = newArray;
DesplazamientoOrigen = newArray;



//ESTE BUCLE FOR SIRVE PARA COGER LAS 3 FUENTES DE UNA EN UNA. TRAS ELEGIR UNA FUENTE, HACE TODOS LOS CÁLCULOS Y PASAMOS A LA SIGUIENTE
for(p=1; p<=3; p++){
	print("");
	print("fuente numero " + p + "");
	xijm = 0;
	yijm = 0;
	
	//EL BUCLE FOR SE ENCARGA DE HACER UN AJUSTE GAUSSIANO A CADA ROI DE CADA IMAGEN TOMADA POR EL SPECT
	for(k=slice_inicial; k<=slice_final; k++){
		//EL BUCLE IF TIENE EN CUENTA SI AUN NO HEMOS ESPECIFICADO DONDE COLOCAR LA ROI, HACIENDO QUE SOLO TENGAMOS QUE CREARLA PARA LA PRIMERA IMAGEN
		if(k == slice_inicial){
			//EL BUCLE DO WHILE NO NOS PERMITIRÁ ESCOGER UNA ROI INCORRECTA
			do {

				//ESTOS BUCLES IF VAN A SELECCIONAR DISTINTOS VALORES DE Y PARA LA ROI DE LAS DISTINTAS FUENTES 1, 2 Y 3
				if(p==1){
					ysugerido = 50;
				}
				else if(p==2){
					ysugerido = 100;
				}
				else{
					ysugerido = 150;
				}
				
				//ESPECIFICACIONES DE LA ROI. DOY A ELEGIR TAMAÑO Y POSICION DE LA ROI
				Dialog.create("Fuente n." + p + " - Elija ROI");
				Dialog.addMessage("Valores sugeridos                                     ");
				Dialog.addNumber("width: ", matrizpixeles);
				Dialog.addNumber("height: ", 60);
				Dialog.addNumber("x: ", 0);
				Dialog.addNumber("y: ", ysugerido);
				Dialog.show();
				ROI_width = Dialog.getNumber();
				ROI_height = Dialog.getNumber();
				ROI_x = Dialog.getNumber();
				ROI_y= Dialog.getNumber();

				print("");
				print("width = "+ ROI_width + " ");
				print("height = "+ ROI_height + " ");
				print("x = "+ ROI_x + " ");
				print("y = "+ ROI_y + " ");
	
				
				//Selección de la ROI
				run("Specify...", "width=" +ROI_width +" height=" + ROI_height +" x=" + ROI_x +" y=" + ROI_y +" slice=" + k + "");
			
				//CONFIRMACION VISUAL DE SI LA ROI ESTA DONDE TOCA. EN CASO NEGATIVO, FUERZO A ELEGIR ROI NUEVA
				Dialog.create("Fuente n." + p + "  - ¿ROI bien colocada?");
				Dialog.addMessage("ROI bien = 1 , ROI mal = 0                   ");
				Dialog.addNumber(" ", 0);
				Dialog.show();
				ROI = Dialog.getNumber();
	
				if(ROI > 1){
					ROI = 0;
				}
			
				//AVISO DE QUE LA ROI SE SALE DE LA MATRIZ EN X Y FUERZO A ELEGIR ROI NUEVA
				ROI_width_x = ROI_width + ROI_x;
				if(ROI_width_x > matrizpixeles){
					Dialog.create("ROI sale de matriz");
					Dialog.addMessage("ROI sale de matriz en el eje x");
					ROI = 0;
					Dialog.show();
				}
			
				//AVISO DE QUE LA ROI SE SALE DE LA MATRIZ EN Y Y FUERZO A ELEGIR ROI NUEVA
				ROI_height_y = ROI_height + ROI_y;
				if(ROI_height_y > matrizpixeles){
					Dialog.create("ROI sale de matriz");
					Dialog.addMessage("ROI sale de matriz en el eje y");
					ROI = 0;
					Dialog.show();
				}
			
			
				//print ("el valor de ROI es = " + ROI + "");
				}while(ROI == 0)
				//CERRADO EL BUCLE DO
		}
	
		else{
			run("Specify...", "width=" + ROI_width +" height=" + ROI_height +" x=" + ROI_x +" y=" + ROI_y +" slice=" + k + "");
		}
		ROI = 0;
		//CERRADO EL BUCLE IF (K == slice_inicial)
	
		//HACE UN PLOT PARA CADA IMAGEN EN LA ROI SELECCIONADA Y AJUSTA EL VALOR DE GRIS A UNA GAUSSIANA. DA EL VALOR DE LA POSICIÓN DEL MÁXIMO EN X
		run("Plot Profile");
		Plot.getValues(xValues, grayValues);
		Fit.doFit("Gaussian", xValues, grayValues);
		Fit.plot;
		//posx = newArray;
		posx = Fit.p(2) + (ROI_x + 0.5)*matrizmm/matrizpixeles;
		GrayValue = Fit.p(1);
	
		//ESTOS BUCLES IF SIRVEN PARA VER SI LA ROI NO CUBRE BIEN LA POSICIÓN DE LA FUENTE
		if(GrayValue<0.15){
			print("");
			print("el maximo en x tiene un valor de " + GrayValue + "en la imagen " + k + "");
			Dialog.create("ROI mal");
			Dialog.addMessage("Asegurate de que la ROI esta bien colocada, falla en la imagen " + k + "");
			Dialog.show();
		}
		else if(GrayValue>10000){
			print("");
			print("el maximo en x tiene un valor de " + GrayValue + "en la imagen " + k + "");
			Dialog.create("ROI mal");
			Dialog.addMessage("Asegurate de que la ROI esta bien colocada, falla en la imagen " + k + "");
			Dialog.show();
		}
		//print("la posicion del maximo en x es " + posx + " en la imagen " + k + "");
		
		close();
		close();
		
		//A LA VEZ QUE CALCULAMOS LA GAUSSIANA DE LOS PUNTOS EN X, TAMBIEN LO HACEMOS PARA LOS PUNTOS EN Y
		setKeyDown("alt"); run("Plot Profile");
		Plot.getValues(yValues, ygrayValues);
		Fit.doFit("Gaussian", yValues, ygrayValues);
		Fit.plot;
		posy = Fit.p(2) + (ROI_y + 0.5)*matrizmm/matrizpixeles;
		GrayValuey = Fit.p(1);
		
	
		//ESTOS BUCLES IF SIRVEN PARA VER SI LA ROI NO CUBRE BIEN LA POSICIÓN DE LA FUENTE
		if(GrayValuey<0.1){
			print("");
			print("el maximo en y tiene un valor de " + GrayValuey + "en la imagen " + k + "");
			Dialog.create("ROI mal");
			Dialog.addMessage("Asegurate de que la ROI esta bien colocada, falla en la imagen " + k + "");
			Dialog.show();
		}
		else if(GrayValuey>1000){
			print("");
			print("el maximo en y tiene un valor de " + GrayValuey + "en la imagen " + k + "");
			Dialog.create("ROI mal");
			Dialog.addMessage("Asegurate de que la ROI esta bien colocada, falla en la imagen " + k + "");
			Dialog.show();
		}
	
		//print("la posicion del maximo en y es " + posy + " en la imagen " + k + "");
		
		close();
		close();
		
		//GUARDAMOS LAS POSICIONES X E Y EN UN ARRAY (MATRIZ)
		xArray[k] = posx;
		yArray[k] = posy;
		slice[k] = k;
		xijm = xijm + posx;
		yijm = yijm + posy;
		//print("la posicion del maximo es " + xArray[k] + " en la imagen " + k + "");
		//print ("el valor de xijm es " + xijm + "");
		//print ("el valor de xvalues es " + xvalues[k] + "");
		
	
	}
	//AQUI ACABA EL BUCLE FOR QUE VA COGIENDO TODOS LOS MÁXIMOS DE LA GAUSSIANA CORTE A CORTE
	
	//SE CALCULA EL VALOR MEDIO DEL EJE Y QUE LA MEDIA DE DESVIACIONES NO SEA MAYOR DE MEDIO PIXEL Y QUE LA DESVIACION MAXIMA NO SEA MAYOR QUE 1 PIXEL
	
	CORy = yijm/ROI_slice_max;
	print("");
	print("El valor medio de Y es " + CORy*matrizpixeles/matrizmm + " pixeles (Centro de masas)");
	Plot.create("title", "slice", "posicion y");
	Plot.add("circle", slice, yArray);
	Plot.update();
	
	diferenciamax = 0;
	diferenciasuma = 0;
	
	for(k=slice_inicial; k<=slice_final; k++){
		diferencia = abs(yArray[k]-CORy);
		diferenciasuma = diferenciasuma + diferencia;
		if(diferencia > diferenciamax){
		diferenciamax = diferencia; 
		puntoy = k;
		}
	}
	diferenciamedia = diferenciasuma/ROI_slice_max;

	print("El valor medio de diferencia entre los puntos del eje Y y su valor medio es de " + diferenciamedia*matrizpixeles/matrizmm + " pixeles");
	print("El valor maximo de diferencia entre los puntos del eje Y y su valor medio es de " + diferenciamax*matrizpixeles/matrizmm + " pixeles");
	
	Dialog.create("Fuente n." + p + " - Eje Y. Centro de masas");
	Dialog.addMessage("El valor medio de Y es " + CORy*matrizpixeles/matrizmm + " pixeles (Centro de masas)");
	Dialog.addMessage("El valor medio de diferencia entre los puntos del eje Y y su valor medio es de " + diferenciamedia*matrizpixeles/matrizmm + " pixeles");
	Dialog.addMessage("El limite de tolerancia es 0.5 pixeles");
	Dialog.addMessage("El valor maximo de diferencia entre los puntos del eje Y y su valor medio " + diferenciamax*matrizpixeles/matrizmm + " pixeles");
	Dialog.addMessage("Vigilar si este valor es mayor de 1 pixel");
	Dialog.show();
	
	
	//SE CALCULA EL COR
	//print("El valor de k es " + k + "");
	COR = xijm/ROI_slice_max;
	CORpixel = COR*matrizpixeles/matrizmm;
	//CORcomparacion[p] = CORpixel;
	print("");
	//print("El valor de COR es " + COR + " mm (Centro de masas)");
	print("El valor de COR es " + CORpixel + " pixeles (Centro de masas)");
	ResiduoCOR = abs(CORpixel - matrizpixeles/2);
	print("La diferencia entre el COR y el centro es de " + ResiduoCOR + " pixeles");
	//SE PLOTEA LA MEDIDA DE LA POSICIÓN DE LA FUENTE EN FUNCIÓN DEL CORTE
	Plot.create("title", "slice", "posicion x");
	Plot.add("circle", slice, xArray);
	Plot.update();
	Dialog.create("Fuente n." + p + " - Eje  X. Centro de masas");
	Dialog.addMessage("El COR esta en la posicion " + CORpixel + " pixeles (Centro de masas)");
	Dialog.addMessage("La diferencia entre el COR y el centro de la matriz es de " + ResiduoCOR + " pixeles");
	Dialog.addMessage("La tolerancia es de 0.5 pixeles");
	Dialog.show();
	
	
	
	//SE HACE UN FIT A UN SENO
	fit = 0;
	a = COR;
	b = 100;
	c = 0;
	d = 0;
	do{
	
	initialGuesses = newArray(a, b, c, d);
	Fit.doFit("y = a + b*sin(c*x+d)", slice, xArray,initialGuesses);
	Fit.plot;
	sinA = Fit.p(0);
	sinB = Fit.p(1);
	sinC = Fit.p(2);
	sinD = Fit.p(3);
	DesplazamientoOrigen[p] = sinA*matrizpixeles/matrizmm;
	

	BooleanoSeno = 0;
	BooleanoMensaje = 0;
	Dialog.create("Fuente n." + p + "");
	Dialog.addMessage( "Compruebe que el fit del seno es correcto. En caso de que no sea correcto, ponga un 0 en la siguiente casilla");
	Dialog.addNumber("Seno correcto ", 1);
	Dialog.addMessage("Indique la tolerancia en pixeles de la desviacion de la posicion de la fuente con respecto al fit sinusoidal");
	Dialog.addMessage("El limite de tolerancia es 1 pixel");
	Dialog.addNumber("tolerancia en pixeles: ", 0.5);
	Dialog.show();
	fit = Dialog.getNumber();
	ToleranciaSeno = Dialog.getNumber();
	print("");
	if(fit == 1){
		//print("la tolerancia de la desviacion de la posicion del pixel con respecto al fit sinusoidal es de " + ToleranciaSeno + " pixeles");
	}

	else{
		Dialog.create("Fuente n." + p + "");
		Dialog.addMessage( "Es necesario cambiar los valores iniciales del plot del seno");
		Dialog.addNumber("a (COR) - en principio no habria que modificarlo -: ", a);
		Dialog.addNumber("b - en principio no habria que modificarlo -: ", b);
		Dialog.addNumber("c - sugiero cambiar c o d al valor de 1 ", c);
		Dialog.addNumber("d - sugiero cambiar c o d al valor de 1 ", d);
		Dialog.show();
		a = Dialog.getNumber();
		b = Dialog.getNumber();
		c = Dialog.getNumber();
		d = Dialog.getNumber();
		fit = 0;
	}
	close();
	} while(fit == 0)
	close();

		
	DesvSinSuma = 0;
	DesvSinMax_final = 0;

	//ESTE BUCLE FOR SIRVE PARA VER CUÁNTO SE DESVÍA CADA VALOR MEDIDO CON EL VALOR DEL FIT DEL SENO (EN CASO DE DESVIARSE MÁS QUE LA TOLERANCIA)
	for(k=slice_inicial; k<=slice_final; k++){
	//ESTO ES PARA FORZAR A QUE FALLEN UN PAR DE PUNTOS 
//		if(k == slice_inicial + 2){
//			xArray[k] = 0;
//		}
//		if(k == slice_inicial + 27){
//			xArray[k] = 0;
//		}
		DesvSin = abs(sinA + sinB*sin(sinC*k+sinD) - xArray[k]);
		DesvSinPixel = DesvSin*matrizpixeles/matrizmm;
		DesvSinSuma = DesvSinSuma + DesvSinPixel;
		//print("en la imagen " + k + " la desviacion en mm del punto con el seno es " + DesvSin + "");
		//print("en la imagen " + k + " la desviacion en pixeles del punto con el seno es " + DesvSinPixel + "");
		if(DesvSinPixel >= ToleranciaSeno){
			BooleanoSeno = BooleanoSeno + 1;
			print("en la imagen " + k + ", la desviacion de la posicion del pixel con respecto al fit sinusoidal es de " + DesvSinPixel + " pixeles");
			if(BooleanoMensaje == 0){
				
				Dialog.create("Fuente n." + p + " - Fuera tolerancia");
				Dialog.addMessage("en la imagen " + k + ", la desviacion de la posicion del pixel con respecto al fit sinusoidal es de " + DesvSinPixel + " pixeles (mayor de " + ToleranciaSeno + " pixeles)");
				Dialog.addMessage("si desea que este mensaje deje de salir, ponga un valor distinto de 0");
				Dialog.addNumber("valor: ", 0);
				Dialog.show();
				BooleanoMensaje = Dialog.getNumber();
			}
		}
		
		if(DesvSinPixel > DesvSinMax_final){
				DesvSinMax_final = DesvSinPixel; 
			}
	}


	//CALCULAMOS EL COR CON EL FIT DEL SENO Y COMPARAMOS CON EL CENTRO DE MASAS
	
	//print("El valor de COR es " + sinA + " mm (Fit seno)");
	sinApixel = sinA*matrizpixeles/matrizmm;
	print("El COR esta en la posicion " + sinApixel + " pixeles (Fit seno)");
	print("La diferencia entre el COR y el centro de la matriz es de " + abs(sinApixel - matrizpixeles/2) + " pixeles");
	print("");
	print("La diferencia en el COR para ambos metodos (Centro de masas y Fit seno) es de " + abs(sinApixel - CORpixel) + " pixeles");
	Dialog.create("Fuente n." + p + " - Eje  X. Fit seno");
	Dialog.addMessage("El COR esta en la posicion " + sinApixel + " pixeles (Fit seno)");
	Dialog.addMessage("La diferencia entre el COR y el centro de la matriz es de " + abs(sinApixel - matrizpixeles/2) + " pixeles");
	Dialog.addMessage("La tolerancia es de 0.5 pixeles");
	Dialog.addMessage("");
	Dialog.addMessage("La diferencia en el COR para ambos metodos (Centro de masas y Fit seno) es de " + abs(sinApixel - CORpixel) + " pixeles");
	Dialog.show();
	
	//ESTE DIALOG NOS DICE CUÁNTOS PUNTOS SE DESVÍAN MÁS DE 1 PIXEL DEL FIT DEL SENO (O LA DESVIACIÓN QUE HAYAMOS ESCOGIDO) Y LA MEDIA EN PÍXELES DE TODAS LAS DESVIACIONES CON EL FIT DEL SENO (RESIDUO)
	
	print("");
	//print("La tolerancia de la desviacion de la posicion del pixel con respecto al fit sinusoidal es de " + ToleranciaSeno + " pixeles");
	print("Hay " + BooleanoSeno + " posiciones de la fuente desviadas mas de " + ToleranciaSeno + " pixeles");
	print("La desviacion maxima es de " + DesvSinMax_final + " pixeles");
	print("El valor limite de esta desviacion es de 1 pixel");
	DesvSinMedia = DesvSinSuma/ROI_slice_max;
	print("La desviacion media de cada punto con respecto al ajuste de la sinusoide (RESIDUO) es de " + DesvSinMedia + " pixeles");
	print("La tolerancia es de 0.5 pixeles");
	Dialog.create("Fuente n." + p + " - Resumen seno");
	Dialog.addMessage("Hay " + BooleanoSeno + " posiciones de la fuente desviadas mas de " + ToleranciaSeno + " pixeles");
	Dialog.addMessage("La desviacion maxima es de " + DesvSinMax_final + " pixeles");
	Dialog.addMessage("El valor limite de esta desviacion es de 1 pixel");
	Dialog.addMessage("");
	Dialog.addMessage("La desviacion media de cada punto con respecto al ajuste de la sinusoide (RESIDUO) es de " + DesvSinMedia + " pixeles");
	Dialog.addMessage("La tolerancia es de 0.5 pixeles");
	Dialog.show();


}
//SE CIERRA EL BUCLE FOR QUE ANALIZA LAS 3 FUENTES


//ANALIZAMOS EL DESPLAZAMIENTO DEL ORIGEN DEL FIT DEL SENO DE LAS 3 FUENTES Y LAS COMPARAMOS
DOmin = 999990;
DOmax = 0;
for(p=1; p<=3; p++){
	if(DesplazamientoOrigen[p] < DOmin){
		DOmin = DesplazamientoOrigen[p]; 
	}
	if(DesplazamientoOrigen[p] > DOmax){
		DOmax = DesplazamientoOrigen[p];
	}
}
DOdiferencia = DOmax - DOmin;

print("");
print("El valor de desplazamiento del origen en el seno en pixeles es " + DesplazamientoOrigen[1] + " para la fuente " + 1 + "");
print("El valor de desplazamiento del origen en el seno en pixeles es " + DesplazamientoOrigen[2] + " para la fuente " + 2 + "");
print("El valor de desplazamiento del origen en el seno en pixeles es " + DesplazamientoOrigen[3] + " para la fuente " + 3 + "");
print("La diferencia en pixeles del desplazamiento maximo y minimo es de " + DOdiferencia + ". La tolerancia es de 0.5 pixeles");

Dialog.create("Desplazamiento del origen - fit seno");
Dialog.addMessage("El valor de desplazamiento del origen en el seno en pixeles es " + DesplazamientoOrigen[1] + " para la fuente " + 1 + "");
Dialog.addMessage("El valor de desplazamiento del origen en el seno en pixeles es " + DesplazamientoOrigen[2] + " para la fuente " + 2 + "");
Dialog.addMessage("El valor de desplazamiento del origen en el seno en pixeles es " + DesplazamientoOrigen[3] + " para la fuente " + 3 + "");
Dialog.addMessage("La diferencia en pixeles del desplazamiento maximo y minimo es de " + DOdiferencia + ". La tolerancia es de 0.5 pixeles");
Dialog.show();
