function buildCategories(fullSubjectName) 

%Retrieve a signal within this subject
tempSubject = evalin('base', fullSubjectName);
fieldsNames = fieldnames(tempSubject);
for i=1:length(fieldsNames)
    channelName = fieldsNames{i};
    eval(['isSignal = tempSubject.', channelName, '.isSignal;']);
    if(isSignal)
        break;
    end
end

%Get nb trials from this signal
eval(['tempSize = size(tempSubject.', channelName, '.Values);']);
eval('nbTrials = tempSize(1);');

%Get nb Categories
nbCategories = 0;
for i=1:length(fieldsNames)
    channelName = fieldsNames{i};
    eval(['isCategory = tempSubject.', channelName, '.isCategory;']);
    if(isCategory)
        nbCategories = nbCategories + 1;
    end
end

if(nbCategories > 0)
    
    %Retrieve category name for each trial
    for currentTrial=1:nbTrials
        Categories(currentTrial) = {'empty label'};
    end
    for currentTrial=1:nbTrials    
        for currentCategory=1:nbCategories
            eval(['trialsList = tempSubject.Category', num2str(currentCategory), '.TrialsList;']);
            if(isempty(find(trialsList == currentTrial)) == 0)
                eval(['criteria = tempSubject.Category', num2str(currentCategory), '.Criteria;']);
                Categories(currentTrial) = {criteria};
                break;
            end
        end        
    end
    
    %Apply in base workspace
    tempSubject.Categories.Names = Categories;
    tempSubject.Categories.isCategory = 0;
    tempSubject.Categories.isSignal = 0;
    tempSubject.Categories.isEvent = 0;   
    assignin('base','tempSubject',tempSubject);
    evalin('base', [fullSubjectName,'=tempSubject;']);
    evalin('base','clear tempSubject;');    

end



