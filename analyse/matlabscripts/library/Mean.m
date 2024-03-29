%Makes the average of the samples of one signal
%between two markers
function Mean (TrialsList , signalsNamesList, markersNamesList, fieldsNamesList, signalsNamesSuffix, newMarkersNamesList, newFieldsNamesList, signalsModifiedNumber)
%beginAnalyseHeader
%GUIFunctionName = Mean
%MatlabFunctionName = Mean
%ShortDescritpion = <p>Samples mean between two markers</p>
%LongDescription = <p>Makes the average of the samples of one signal between two markers</p>
%SignalsUsedNumber = 1
%MarkersUsedNumber = 2
%FieldsUsedNumber = 0
%SignalsCreatedNumber = 0
%MarkersCreatedNumber = 0
%fieldsCreatedNumber = 1
%signalsModifiedNumber = 0
%endAnalyseHeader
%
% This is the body function
%

%Retrieve error from preceding function
evalin('base','errMsgExists = exist(''errorMessageString'',''var'');');
errMsgExists = evalin('base','errMsgExists');
if(errMsgExists)
    errorMessageString = char('errorMessage;');
    errorMessage = evalin('base',errorMessageString);
else
    errorMessage = '';
end
evalin('base','clear errMsgExists;');
errorID = [mfilename '.m'];
errorMessage = [errorMessage ':' mfilename '.m -> '];

%Check for inputs possiblities
haveSignals = 0;
haveMarkers = 0;
haveFields = 0;
%Check if we have signals and/or markers and/or fields as inputs
%Print error and exit if we have nothing
if(isempty(signalsNamesList))
	if(isempty(markersNamesList))
		if(isempty(fieldsNamesList))
            errorMessage = [errorMessage ...
                'Sorry but can''t create something from nothing !'];
            assignin('base', 'errorMessage', errorMessage);
			error(errorID,'Sorry but can''t create something from nothing !');
			return;
		end;
	end;
end;
if(~isempty(signalsNamesList)) haveSignals = 1; end;
if(~isempty(markersNamesList)) haveMarkers = 1; end;
if(~isempty(fieldsNamesList)) haveFields = 1; end;
%For debugging
%haveSignals
%haveMarkers 
%haveFields

%Check for ouputs possiblities
createSignals = 0;
createMarkers = 0;
createFields = 0;
% %We only have the possiblity to create signals OR markers OR fields
% %And we need to create something
% %Print error and exit if not in this case
% if(~isempty(signalsNamesSuffix))
%     if(~isempty(newMarkersNamesList)) 
%         error(errorID,'Can''t create multiple output types');
%         return;
%     end;
%     if(~isempty(newFieldsNamesList)) 
%         error(errorID,'Can''t create multiple output types');
%         return;
%     end;
%     createSignals = 1;
% else
%     if(~isempty(newMarkersNamesList))
%         if(~isempty(newFieldsNamesList)) 
%             error(errorID,'Can''t create multiple output types');
%             return;
%         end;
%         createMarkers= 1;
%     else
%         if(isempty(newFieldsNamesList)) 
%             error(errorID,'Can''t create nothing !');
%             return;
%         end;
%         createFields = 1;
%     end
% end
% %For debugging
% %createSignals
% %createMarkers
% %createFields
%Check if we have signals and/or markers and/or fields as outputs
%Print error end exit if we have nothing
if(isempty(signalsNamesSuffix) & (signalsModifiedNumber == 0))
	if(isempty(newMarkersNamesList)) 
		if(isempty(newFieldsNamesList)) 
            errorMessage = [errorMessage 'Must create something !'];
            assignin('base', 'errorMessage', errorMessage);
			error(errorID,'Must create something !');
			return;
		end;
	end;
end;
if(~isempty(signalsNamesSuffix)) createSignals = 1; end;
if(~isempty(newMarkersNamesList)) createMarkers = 1; end;
if(~isempty(newFieldsNamesList)) createFields = 1; end;
%For debugging
%createSignals
%createMarkers
%createFields

%TODO :
%Check for creation types : as an exemple,
%check that we don't create signals from fields,
%in other words check that we don't have only fields as inputs
%and signals as ouputs...

%Create inputs signals names cell if necessary
signalsInUpletSize = 0;
signalsInNbUplets = 0;
if(haveSignals)

	%Create input cell InputSignalsNames that holds signals names
	%InputSignalsNames{1}(1) uplet 1, first signal name in uplet
	%InputSignalsNames{1}(2) uplet 1, second signal name in uplet
	%InputSignalsNames{2}(1) uplet 2, first signal name in uplet
	%etc.
	[upletNamesList signalsInNbUplets] = explode(signalsNamesList,':');
	for i=1:signalsInNbUplets
		[namesList signalsInUpletSize] = explode(upletNamesList{i},',');
		InputSignalsNames{i} = namesList;
	end
	%For debugging
	%     signalsInNbUplets
	%     signalsInUpletSize
	%     for i=1:signalsInNbUplets
	%         for j=1:signalsInUpletSize
	%             InputSignalsNames{i}(j)
	%         end
	%     end

end

%Create inputs markers names cell if necessary
markersInUpletSize = 0;
markersInNbUplets = 0;
if(haveMarkers) 

	%Create input cell InputMarkersNames that holds markers names
	%InputMarkersNames{1}(1) uplet 1, first marker name in uplet
	%InputMarkersNames{1}(2) uplet 1, second marker name in uplet
	%InputMarkersNames{2}(1) uplet 2, first marker name in uplet
	%etc.
	[upletNamesList markersInNbUplets] = explode(markersNamesList,':');
	for i=1:markersInNbUplets
		[namesList markersInUpletSize] = explode(upletNamesList{i},',');
		InputMarkersNames{i} = namesList;
	end
	%For debuggingchar
	%markersInNbUplets
	%markersInUpletSize
	%for i=1:markersInNbUplets
	%    for j=1:markersInUpletSize
	%        InputMarkersNames{i}(j)
	%    end
	%end

end

%Create inputs fields names cell if necessary
fieldsInUpletSize = 0;
fieldsInNbUplets = 0;
if(haveFields) 

	%Create input cell InputFieldsNames that holds fields names
	%InputFieldsNames{1}(1) uplet 1, first field name in uplet
	%InputFieldsNames{1}(2) uplet 1, second field name in uplet
	%InputFieldsNames{2}(1) uplet 2, first field name in uplet
	%etc.
	[upletNamesList fieldsInNbUplets] = explode(fieldsNamesList,':');
	for i=1:fieldsInNbUplets
		[namesList fieldsInUpletSize] = explode(upletNamesList{i},',');
		InputFieldsNames{i} = namesList;
	end
	%For debugging
	%fieldsInNbUplets
	%fieldsInUpletSize
	%for i=1:fieldsInNbUplets
	%    for j=1:fieldsInUpletSize
	%        InputFieldsNames{i}(j)
	%    end
	%end

end

%We are working on inputs only...
%If we have signals, master loop has to be done on signals
%else if we have markers, master loop has to be done on markers
%else if we have fields, master loop has to be done on fields
if(haveSignals) MaxMasterLoopIndex = signalsInNbUplets;
elseif(haveMarkers) MaxMasterLoopIndex = markersInNbUplets; 
elseif(haveFields) MaxMasterLoopIndex = fieldsInNbUplets; end;

%Check for uplet inputs conditions
%If master loop on signals uplets
%  signalsInNbUplets > markersInNbUplets & fieldsInNbUplets
%If master loop on markers uplets
%  markersInNbUplets > fieldsInNbUplets
%If master loop on fields uplets
%  no conditions
if(haveSignals)
	if(signalsInNbUplets < markersInNbUplets) 
        errorMessage = [errorMessage ...
             'There are more uplets in markers than in signals inputs'];
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'There are more uplets in markers than in signals inputs'); 
		return;
	end;
	if(signalsInNbUplets < fieldsInNbUplets) 
        errorMessage = [errorMessage ...
            'There are more uplets in fields than in signals inputs'];
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'There are more uplets in fields than in signals inputs'); 
		return;
	end;
elseif(haveMarkers)
	if(markersInNbUplets < fieldsInNbUplets) 
        errorMessage = [errorMessage ...
            'There are more uplets in fields than in markers inputs'];
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'There are more uplets in fields than in markers inputs'); 
		return;
	end;
end

%Create outputs signals names cell if necessary
signalsOutUpletSize = 0;
signalsOutNbUplets = 0;
if(createSignals) 

	%Create output array OutputSignalsNamesSuffix that holds signals
	%names suffix. There is only one uplet
	%OutputSignalsNames(1) first signal name in uplet
	%OutputSignalsNames(2) second signal name in uplet
	%etc.
	signalsOutNbUplets = 1;
	[OutputSignalsNamesSuffix signalsOutUpletSize] =...
	explode(signalsNamesSuffix,',');
	%For debugging
	%signalsOutNbUplets
	%signalsOutUpletSize
	%for i=1:signalsOutUpletSize
	%    OutputSignalsNamesSuffix(i)
	%end

end

%Create outputs markers names cell if necessary
markersOutUpletSize = 0;
markersOutNbUplets = 0;
if(createMarkers) 

	%Create output cell OutputMarkersNames that holds markers names
	%OutputMarkersNames{1}(1) uplet 1, first field name in uplet
	%OutputMarkersNames{1}(2) uplet 1, second field name in uplet
	%OutputMarkersNames{2}(1) uplet 2, first field name in uplet
	%etc.
	[upletNamesList markersOutNbUplets] =...
	explode(newMarkersNamesList,':');
	for i=1:markersOutNbUplets
		[namesList markersOutUpletSize] = ...
		explode(upletNamesList{i},',');
		OutputMarkersNames{i} = namesList;
	end
	%For debugging
	%markersOutNbUplets
	%markersOutUpletSize
	%for i=1:markersOutNbUplets
	%    for j=1:markersOutUpletSize
	%        OutputMarkersNames{i}(j)
	%    end
	%end

end
%Create outputs fields names cell if necessary
fieldsOutUpletSize = 0;
fieldsOutNbUplets = 0;
if(createFields) 

	%Create output cell OutputFieldsNames that holds fields names
	%OutputFieldsNames{1}(1) uplet 1, first field name in uplet
	%OutputFieldsNames{1}(2) uplet 1, second field name in uplet
	%OutputFieldsNames{2}(1) uplet 2, first field name in uplet
	%etc.
	[upletNamesList fieldsOutNbUplets] = ...
	explode(newFieldsNamesList,':');
	for i=1:fieldsOutNbUplets
		[namesList fieldsOutUpletSize] = ...
		explode(upletNamesList{i},',');
		OutputFieldsNames{i} = namesList;
	end
	%For debugging
	%fieldsOutNbUplets
	%fieldsOutUpletSize
	%for i=1:fieldsOutNbUplets
	%    for j=1:fieldsOutUpletSize
	%        OutputFieldsNames{i}(j)
	%    end
	%end

end

%Check for uplet inputs/outputs conditions
%If master loop on signals uplets inputs
%  signalsInNbUplets > signalsOutNbUplets &
%                      markersOutNbUplets & fieldsOutNbUplets
%If master loop on markers uplets inputs
%  markersInNbUplets > markersOutNbUplets & fieldsOutNbUplets
%If master loop on fields uplets inputs
%  fieldsInNbUplets > fieldsOutNbUplets
if(haveSignals)
	if(signalsInNbUplets < signalsOutNbUplets) 
		%Probably impossible as we supply suffix for signal outs, but...
        errorMessage = [errorMessage ...
            'There are more uplets in signal out than in signals in'];
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'There are more uplets in signal out than in signals in'); 
		return;
	end;
	if(signalsInNbUplets < markersOutNbUplets) 
        errorMessage = [errorMessage ...
            'There are more uplets in markers out than in signals in'];
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'There are more uplets in markers out than in signals in'); 
		return;
	end;
	if(signalsInNbUplets < fieldsOutNbUplets) 
        errorMessage = [errorMessage ...
            'There are more uplets in fields out than in signals in'];
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'There are more uplets in fields out than in signals in'); 
		return;
	end;
elseif(haveMarkers)
	if(markersInNbUplets < markersOutNbUplets) 
        errorMessage = [errorMessage ...
            'There are more uplets in markers out than in markers in'];
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'There are more uplets in markers out than in markers in'); 
		return;
	end;
	if(markersInNbUplets < fieldsOutNbUplets) 
        errorMessage = [errorMessage ...
            'There are more uplets in fields out than in markers in'];
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'There are more uplets in fields out than in markers in'); 
		return;
	end;
	if(createMarkers | createSignals)
       errorMessage = [errorMessage ...
           'Can''t create markers or signals without signals'];  
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'Can''t create markers or signals without signals');
		return;
	end;
else
	if(fieldsInNbUplets < fieldsOutNbUplets) 
        errorMessage = [errorMessage ...
            'There are more uplets in fields out than in fields in'];
        assignin('base', 'errorMessage', errorMessage);
		error(errorID,'There are more uplets in fields out than in fields in'); 
		return;
	end;
end

%These variables are to notify GUI at the end of process
%We retreive already created channels if exist
evalin('base','csnExists = exist(''createdSignalsNames'',''var'');');
csnExists = evalin('base','csnExists');
if(csnExists)
    createdSignalsNamesString = char('createdSignalsNames');
    createdSignalsNames = evalin('base',createdSignalsNamesString);
else
    createdSignalsNames = '';
end
evalin('base','clear csnExists;');
evalin('base','cmnExists = exist(''createdMarkersNames'',''var'');');
cmnExists = evalin('base','cmnExists');
if(cmnExists)
    createdMarkersNamesString = char('createdMarkersNames');
    createdMarkersNames = evalin('base',createdMarkersNamesString);
else
    createdMarkersNames = '';
end
evalin('base','clear cmnExists;');
evalin('base','cfnExists = exist(''createdFieldsNames'',''var'');');
cfnExists = evalin('base','cfnExists');
if(cfnExists)
    createdFieldsNamesString = char('createdFieldsNames');
    createdFieldsNames = evalin('base',createdFieldsNamesString);
else
    createdFieldsNames = '';
end
evalin('base','clear cfnExists;');
evalin('base','msnExists = exist(''modifiedSignalsNames'',''var'');');
msnExists = evalin('base','msnExists');
if(msnExists)
    modifiedSignalsNamesString = char('modifiedSignalsNames');
    modifiedSignalsNames = evalin('base',modifiedSignalsNamesString);
else
    modifiedSignalsNames = '';
end
evalin('base','clear msnExists;');

%Master loop    
for masterLoopIndex = 1:MaxMasterLoopIndex
%We are working on uplet number masterLoopIndex

%Create Inputs variables from base data model in function workspace
% for user loop :
%InputSignal1, InputSignal2, ...
%InputMarker1, InputMarker2, ...
%InputField1, InputField2, ...
%Can't use nested function because we add variables dynamicaly
if(haveSignals)    
	%Get signals from uplet number masterLoopIndex
	signalsUpletNumber = masterLoopIndex;
	for i=1:signalsInUpletSize         
		signalFullName = char(InputSignalsNames{signalsUpletNumber}(i));
		InputSignalTemp = evalin('base',signalFullName);
		eval(['InputSignal' int2str(i) ' = InputSignalTemp;']);        
	end          
	clear InputSignalTemp;
	%For debugging
	%     InputSignal1
	%     if(signalsInUpletSize > 1) 
	%         InputSignal2 
	%     end;
	%     if(signalsInUpletSize > 2) 
	%         InputSignal3
	%     end;
end
if(haveMarkers)
	%Here we have values of markers : E.S.S.MarkerN_Values
	%Get markers from uplet number 
	%masterLoopIndex modulus markersInNbUplets
	if(~haveSignals)
		markersUpletNumber = masterLoopIndex;
	else
		markersUpletNumber = mod(masterLoopIndex-1,...
		markersInNbUplets) + 1;
		trialsNumber = eval('size(InputSignal1.Values,1);');
		samplesNumber = eval('size(InputSignal1.Values,2);');
	end
	for i=1:markersInUpletSize         
		markerFullName = ...
		char(InputMarkersNames{markersUpletNumber}(i));
		if(strcmp('0',markerFullName))
			InputMarkerTemp = zeros(trialsNumber,3);
			InputMarkerTemp(:,1)=1:trialsNumber;
			InputMarkerTemp(:,3)=eval('InputSignal1.Values(:,1);');
			eval(['InputMarker' int2str(i) '.Values = InputMarkerTemp;']);
			eval(['InputMarker' int2str(i) '.Label = ''signalStart'';']);
		elseif(strcmp('Inf',markerFullName))
			InputMarkerTemp = zeros(trialsNumber,3);
			InputMarkerTemp(:,1)=1:trialsNumber;
			InputMarkerTemp(:,2)=eval('(samplesNumber-1)/InputSignal1.SampleFrequency;');
			InputMarkerTemp(:,3)=eval('InputSignal1.Values(:,samplesNumber);');
			eval(['InputMarker' int2str(i) '.Values = InputMarkerTemp;']);
			eval(['InputMarker' int2str(i) '.Label = ''signalStop'';']);
		else
			InputMarkerTemp = evalin('base',[markerFullName '_Values']);   
			eval(['InputMarker' int2str(i) '.Values = InputMarkerTemp;']);      
			InputMarkerTemp = evalin('base',[markerFullName '_Label']);   
			eval(['InputMarker' int2str(i) '.Label = InputMarkerTemp;']);    
		end
	end          
	clear InputMarkerTemp;
	%For debugging
	%     InputMarker1
	%     if(markersInUpletSize > 1) 
	%         InputMarker2
	%     end;
	%     if(markersInUpletSize > 2) 
	%         InputMarker3
	%     end;
end;
if(haveFields)
	%Here we have values of fields : E.S.S.FieldName
	%Get field from uplet number 
	%masterLoopIndex modulus fieldsInNbUplets
	if(haveSignals | haveMarkers)    
		fieldsUpletNumber = mod(masterLoopIndex-1,fieldsInNbUplets) + 1;
	else
		fieldsUpletNumber = masterLoopIndex;
	end
	for i=1:fieldsInUpletSize         
		fieldFullName = ...
		char(InputFieldsNames{fieldsUpletNumber}(i));
		InputFieldTemp = evalin('base',[fieldFullName '_Values']);   
		eval(['InputField' int2str(i) '.Values = InputFieldTemp;']);       
		InputFieldTemp = evalin('base',[fieldFullName '_Label']);   
		eval(['InputField' int2str(i) '.Label = InputFieldTemp;']);   
	end          
	clear InputFieldTemp;
	%For debugging
	%     InputField1
	%     if(fieldsInUpletSize > 1) 
	%         InputField2
	%     end;
	%     if(fieldsInUpletSize > 2) 
	%         InputField3
	%     end;
end

%Create Outputs variables in function workspace for user loop :
%OutputSignal1, OutputSignal2, ...
%OutputMarker1, OutputMarker2, ...
%OutputField1, OutputField2, ...
%Can't use nested function because we add variables dynamically
if(createSignals)
	signalsOutUpletNumber = ...
	mod(masterLoopIndex-1,signalsOutNbUplets) + 1;    
	TrialsNumber = eval('size(InputSignal1.Values,1);');
	samplesNumber = eval('size(InputSignal1.Values,2);');
	for i = 1:signalsOutUpletSize
		eval(['OutputSignal' int2str(i)...
			  '.Values = zeros(TrialsNumber,samplesNumber);']); 
		eval(['OutputSignal' int2str(i)...
			  '.NbSamples = InputSignal1.NbSamples;']);
		eval(['OutputSignal' int2str(i)...
			  '.FrontCut = InputSignal1.FrontCut;']);                  
		eval(['OutputSignal' int2str(i)...
			  '.EndCut = InputSignal1.EndCut;']);                   
		eval(['OutputSignal' int2str(i)...
			  '.SampleFrequency = InputSignal1.SampleFrequency;']);
		eval(['OutputSignal' int2str(i) '.isSignal = 1;']);
		eval(['OutputSignal' int2str(i) '.isCategory = 0;']);
		eval(['OutputSignal' int2str(i) '.isEvent = 0;']);
		eval(['OutputSignal' int2str(i) '.NbMarkers = 0;']);        
		eval(['OutputSignal' int2str(i) '.NbFields = 0;']);  
	end    
end
if(createMarkers)
	markersOutUpletNumber = mod(masterLoopIndex-1,...
								markersOutNbUplets) + 1;
	for i = 1:markersOutUpletSize
		name = char(OutputMarkersNames{markersOutUpletNumber}(i));
		eval(['OutputMarker' int2str(i) '.Label = '''...
			  name ''';']);                 
		eval(['OutputMarker' int2str(i) '.Values = [];']);
	end                
end
if(createFields)
	fieldsOutUpletNumber = ...
	mod(masterLoopIndex-1,fieldsOutNbUplets) + 1;
	trialsNumber = eval('size(InputSignal1.Values,1);');
	for i = 1:fieldsOutUpletSize
		name = char(OutputFieldsNames{fieldsOutUpletNumber}(i));
		eval(['OutputField' int2str(i) '.Label = ''' name ''';']);
		eval(['OutputField' int2str(i)...
			  '.Values = zeros(trialsNumber,1);']);
	end
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%					Specific function here
%beginFunction
%Loop over trials
for TrialNumber = TrialsList

	fromTrialIndices = find(InputMarker1.Values(:,1)==TrialNumber);
	toTrialIndices = find(InputMarker2.Values(:,1)==TrialNumber);

	if(~isempty(fromTrialIndices) && ~isempty(toTrialIndices))
		%Get from and to indices in indices arrays
		fromIndex = round(InputMarker1.Values(fromTrialIndices(1), 2)*InputSignal1.SampleFrequency+1);
		toIndex =round( InputMarker2.Values(toTrialIndices(1), 2)*InputSignal1.SampleFrequency+1);

		OutputField1.Values(TrialNumber) = mean(InputSignal1.Values(TrialNumber,fromIndex:toIndex));
		
	end

end
%endFunction
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%Create Outputs values in base workspace
if(createSignals)

	for i=1:signalsOutUpletSize
		if(haveSignals)        
			%Get first signal full name in input uplet number
			%signalsUpletNumber to add current suffix
			newSignalFullName = ...
			[char(InputSignalsNames{signalsUpletNumber}(1)) ...
			 char(OutputSignalsNamesSuffix(i))];
			%Get OutputSignal1 in local data variable
			data = eval(['OutputSignal' int2str(i) ';']); 
			%Apply data in base workspace
			assignin('base', 'tempSignal', data);
			evalin('base', [newSignalFullName ' = tempSignal;']); 
			%Add created signal to list
			createdSignalsNames = ...
			[createdSignalsNames ':' newSignalFullName];
		elseif(haveMarkers)            
            errorMessage = [errorMessage ...
                'Create signals from marker is impossible !'];
            assignin('base', 'errorMessage', errorMessage);
            return;
			for j=1:markersInUpletSize
			end
		elseif(haveFields)           
            errorMessage = [errorMessage ...
                'Create signal from field is impossible !'];
            assignin('base', 'errorMessage', errorMessage);
            return;
			for j=1:fieldsInUpletSize
			end
		end              
	end
	evalin('base','clear tempSignal;');  

end
if(createMarkers)

	for i=1:markersOutUpletSize
		if(haveSignals)       
			%Get OutputMarkerN.Values and OutputMarkerN.Label
			%from current marker in data
			data = eval(['OutputMarker' int2str(i) ';']);
			for j=1:signalsInUpletSize
				%Get signal full name
				signalFullName = ...
				char(InputSignalsNames{signalsUpletNumber}(j));
				%Get nb markers
				nbMarkersInGroup = ...
				evalin('base',[signalFullName '.NbMarkers;']);
				%Increment nb markers locally
				nbMarkersInGroup = nbMarkersInGroup + 1;
				%Create e.s.s.MarkerN_Values
				newMarkerFullName = ...
				[signalFullName '.Marker' ...
				 int2str(nbMarkersInGroup) '_Values'];               
				assignin('base', 'tempMarkersGroup', data.Values);
				evalin('base', [newMarkerFullName ...
								' = tempMarkersGroup;']);
				%Create e.s.s.MarkerN_Label
				newMarkerFullName = ...
				[signalFullName '.Marker' ...
				 int2str(nbMarkersInGroup) '_Label'];               
				assignin('base', 'tempMarkersGroup', data.Label);
				evalin('base', [newMarkerFullName ...
								' = tempMarkersGroup;']);
				%Increment nb markers in base workspace
				newMarkerFullName = [signalFullName '.NbMarkers'];               
				assignin('base', 'tempMarkersGroup', nbMarkersInGroup);
				evalin('base', [newMarkerFullName ...
								' = tempMarkersGroup;']);
				%Add created marker to list
				createdMarkersNames = [createdMarkersNames ':'...
									   [signalFullName '.Marker' ...
										int2str(nbMarkersInGroup)]];
			end
		elseif(haveMarkers)
            errorMessage = [errorMessage ...
                'Create marker from marker is impossible !'];
            assignin('base', 'errorMessage', errorMessage);
            return;
			for j=1:markersInUpletSize
			end
		elseif(haveFields)
			errorMessage = [errorMessage ...
                'Create marker from field is impossible !'];
            assignin('base', 'errorMessage', errorMessage);
            return;
			for j=1:fieldsInUpletSize
			end
		end
	end
	evalin('base','clear tempMarkersGroup;');   

end
if(createFields)
	for i=1:fieldsOutUpletSize
		data = eval(['OutputField' int2str(i)]);
		if(haveSignals)        
			for j=1:signalsInUpletSize
				%Get signal full name
				signalFullName = ...
				char(InputSignalsNames{signalsUpletNumber}(j));
                %Get nb fields
				nbFields = ...
				evalin('base',[signalFullName '.NbFields;']);
				%Increment nb fields locally
				nbFields = nbFields + 1;
				%Create e.s.s.FieldN_Values
				newFieldFullName = ...
				[signalFullName '.Field' ...
				 int2str(nbFields) '_Values']; 
				assignin('base', 'tempField', data.Values);
				evalin('base', [newFieldFullName ...
								' = tempField;']);
				 
				%Create e.s.s.FieldN_Label
				newFieldFullName = ...
				[signalFullName '.Field' ...
				 int2str(nbFields) '_Label'];               
				assignin('base', 'tempField', data.Label);
				evalin('base', [newFieldFullName ...
								' = tempField;']);
				%Increment nb fields in base workspace
				newFieldFullName = [signalFullName '.NbFields'];               
				assignin('base', 'tempField', nbFields);
				evalin('base', [newFieldFullName ...
								' = tempField;']);
				%Add created field to list
				createdFieldsNames = [createdFieldsNames ':'...
									   [signalFullName '.Field' ...
										int2str(nbFields)]];
			end
		elseif(haveMarkers)                   
            errorMessage = [errorMessage ...
                'Create field from marker is impossible !'];
            assignin('base', 'errorMessage', errorMessage);
            return;
			for j=1:markersInUpletSize
			end
		elseif(haveFields) 
            errorMessage = [errorMessage ...
                'Create field from field is impossible !'];
            assignin('base', 'errorMessage', errorMessage);
            return;
			for j=1:fieldsInUpletSize
			end
		end
	end
	evalin('base','clear tempField;');   
end

%Apply modified signals in base workspace
if(signalsModifiedNumber)
	signalsModifiedNumber = min(signalsModifiedNumber, signalsInUpletSize);
	for j=1:signalsModifiedNumber
		modifiedSignalFullName = char(InputSignalsNames{masterLoopIndex}(j));
		%Get InputSignal n°j in local data variable
		data = eval(['InputSignal' int2str(j) ';']);
		%Apply data in base workspace
		assignin('base', 'tempSignal', data);
		evalin('base', [modifiedSignalFullName ' = tempSignal;']);
		%Add created signal to list
		modifiedSignalsNames = ...
		[modifiedSignalsNames ':' modifiedSignalFullName];
	end
	evalin('base','clear tempSignal;')
end
end%Master loop

%Notify changes to GUI
assignin('base', 'createdSignalsNames', createdSignalsNames);
assignin('base', 'createdMarkersNames', createdMarkersNames);
assignin('base', 'createdFieldsNames', createdFieldsNames);
assignin('base', 'modifiedSignalsNames', modifiedSignalsNames);

end







